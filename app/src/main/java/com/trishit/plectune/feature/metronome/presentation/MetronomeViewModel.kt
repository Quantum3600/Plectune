package com.trishit.plectune.feature.metronome.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trishit.plectune.feature.metronome.data.MetronomeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val MIN_BPM = 30
private const val MAX_INTERVAL_MS = (60000L / MIN_BPM).toInt() // 2000ms

class MetronomeViewModel : ViewModel() {
    private val engine = MetronomeEngine()

    private val _uiState = MutableStateFlow(MetronomeUiState())
    val uiState = _uiState.asStateFlow()

    private var startJob: Job? = null

    fun onEvent(event: MetronomeEvent) {
        when (event) {
            is MetronomeEvent.TogglePlay -> toggleMetronome()
            is MetronomeEvent.Stop -> stopMetronome()
            is MetronomeEvent.BeginBpmInteraction -> {
                if (_uiState.value.isPlaying) stopMetronome()
            }
            is MetronomeEvent.TapTempo -> handleTapTempo()
            is MetronomeEvent.SetBpm -> updateBpm(event.bpm)
            is MetronomeEvent.AdjustBpm -> updateBpm(_uiState.value.bpm + event.amount)
            is MetronomeEvent.SetTimeSignature -> {
                if (_uiState.value.isPlaying) stopMetronome()
                _uiState.update {
                    it.copy(
                        timeSignature = event.signature,
                        beatInBar = 0,
                        isAccentedBeat = true
                    )
                }
            }
        }
    }

    private fun updateBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(20, 300)
        _uiState.update { it.copy(bpm = clamped) }
    }

    private fun toggleMetronome() {
        if (_uiState.value.isPlaying) stopMetronome() else startMetronome(_uiState.value.bpm)
    }

    private fun startMetronome(bpm: Int) {
        val beatsPerBar = _uiState.value.timeSignature.beatsPerBar

        _uiState.update {
            it.copy(
                isPlaying = true,
                beatInBar = 0,
                isAccentedBeat = true
            )
        }

        startJob?.cancel()
        startJob = viewModelScope.launch {
            engine.start(
                bpm = bpm,
                beatsPerBar = beatsPerBar
            ) { beatInBar, isAccent ->
                onTick(beatInBar = beatInBar, isAccent = isAccent)
            }
        }
    }

    private fun stopMetronome() {
        _uiState.update {
            it.copy(
                isPlaying = false,
                beatInBar = 0,
                isAccentedBeat = true
            )
        }
        startJob?.cancel()
        startJob = null
        engine.stop()
    }

    private fun onTick(beatInBar: Int, isAccent: Boolean) {
        // event-style update; UI runs its own animation smoothly
        _uiState.update {
            it.copy(
                tickId = it.tickId + 1L,
                beatInBar = beatInBar,
                isAccentedBeat = isAccent
            )
        }
    }

    private fun handleTapTempo() {
        _uiState.update { currentState ->
            val currentTime = System.currentTimeMillis()

            if (currentState.tapCount == 0) {
                // First tap: Initialize sequence
                currentState.copy(
                    lastTapTime = currentTime,
                    tapCount = 1,
                )
            } else {
                val interval = currentTime - currentState.lastTapTime

                // Check for timeout (if interval exceeds time needed for 30 BPM)
                if (interval > MAX_INTERVAL_MS) {
                    // Reset sequence, start new sequence with the current tap
                    currentState.copy(
                        lastTapTime = currentTime,
                        tapCount = 1,
                    )
                } else {
                    // Subsequent tap within time limit
                    val calculatedBpm = (60000f / interval).roundToInt()

                    // Clamp the result: Ensure it's at least 30 BPM
                    val newBpm = calculatedBpm.coerceAtLeast(MIN_BPM)

                    // We only update BPM immediately based on the last interval for responsiveness.
                    // Increment tap count to track user effort/stability.
                    currentState.copy(
                        lastTapTime = currentTime,
                        tapCount = currentState.tapCount + 1,
                        bpm = newBpm // Set the new BPM immediately
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMetronome()
    }
}