package com.trishit.plectune.feature.tuner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trishit.plectune.feature.tuner.data.ToneGenerator
import com.trishit.plectune.feature.tuner.data.TunerEngine
import com.trishit.plectune.feature.tuner.domain.GuitarString
import com.trishit.plectune.feature.tuner.domain.MusicTheory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class TunerViewModel : ViewModel() {
    private val tunerEngine = TunerEngine()
    private val toneGenerator = ToneGenerator()
    private var listeningJob: Job? = null
    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: TunerEvent) {
        when (event) {
            is TunerEvent.StartListening -> startTuner()
            is TunerEvent.StopListening -> stopTuner()
            is TunerEvent.SelectString -> {
                // If clicking the same string, toggle back to Auto
                val currentMode = _uiState.value.mode
                if (currentMode is TunerMode.Manual && currentMode.string == event.string) {
                    _uiState.update { it.copy(mode = TunerMode.Auto, activeString = null) }
                } else {
                    _uiState.update { it.copy(mode = TunerMode.Manual(event.string), activeString = event.string) }
                }
            }
            is TunerEvent.SelectString -> {
                val currentMode = _uiState.value.mode

                if (currentMode is TunerMode.Manual && currentMode.string == event.string) {
                    // Clicking the SAME string again -> Just play the tone again
                    playTone(event.string)
                } else {
                    // Switching to a NEW string -> Set Mode AND Play Tone
                    _uiState.update {
                        it.copy(mode = TunerMode.Manual(event.string), activeString = event.string)
                    }
                    playTone(event.string)
                }
            }
            is TunerEvent.ToggleAutoMode -> {
                _uiState.update { it.copy(mode = TunerMode.Auto, activeString = null) }
            }
        }
    }

    private fun startTuner() {
        if (listeningJob?.isActive == true) return

        listeningJob = viewModelScope.launch(Dispatchers.IO) {
            tunerEngine.startListening().collect { frequency ->
                processFrequency(frequency)
            }
        }
    }

    private fun stopTuner() {
        listeningJob?.cancel()
    }

    private fun playTone(string: GuitarString) {
        viewModelScope.launch {
            // Stop listening briefly so the tuner doesn't hear itself (optional but cleaner)
            toneGenerator.playTone(string.frequency)
        }
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator.stop() // Cleanup
    }

    private fun processFrequency(frequency: Float) {
        _uiState.update { state ->
            // 1. Determine Target Frequency (Auto vs Manual)
            val targetHz = if (state.mode is TunerMode.Manual) state.mode.string.frequency else null

            // 2. Calculate Note & Cents
            val result = MusicTheory.getNote(frequency, targetHz)

            // 3. Update History (Keep last 50 points for the graph)
            val newHistory = (listOf(result.cents) + state.pitchHistory).take(50)

            // 4. Auto-Highlight Peg in Auto Mode
            val activeString = if (state.mode is TunerMode.Auto) {
                // Find if the detected note matches a guitar string name (e.g. "E2")
                GuitarString.entries
                    .find { it.noteName == result.name }
            } else {
                state.activeString
            }

            state.copy(
                currentFreq = frequency,
                noteName = result.name,
                centsOff = result.cents,
                isStable = abs(result.cents) < 5, // Green glow threshold
                pitchHistory = newHistory,
                activeString = activeString
            )
        }
    }
}