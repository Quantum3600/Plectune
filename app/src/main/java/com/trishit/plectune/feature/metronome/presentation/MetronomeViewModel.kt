package com.trishit.plectune.feature.metronome.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trishit.plectune.feature.metronome.data.MetronomeEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MetronomeViewModel : ViewModel() {
    private val engine = MetronomeEngine()

    private val _uiState = MutableStateFlow(MetronomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: MetronomeEvent) {
        when (event) {
            is MetronomeEvent.TogglePlay -> toggleMetronome()
            is MetronomeEvent.SetBpm -> updateBpm(event.bpm)
            is MetronomeEvent.AdjustBpm -> updateBpm(_uiState.value.bpm + event.amount)
        }
    }

    private fun updateBpm(newBpm: Int) {
        // Clamp BPM between 20 and 300
        val clamped = newBpm.coerceIn(20, 300)
        _uiState.update { it.copy(bpm = clamped) }

        // If playing, restart engine with new BPM
        if (_uiState.value.isPlaying) {
            stopMetronome()
            startMetronome(clamped)
        }
    }

    private fun toggleMetronome() {
        if (_uiState.value.isPlaying) {
            stopMetronome()
        } else {
            startMetronome(_uiState.value.bpm)
        }
    }

    private fun startMetronome(bpm: Int) {
        _uiState.update { it.copy(isPlaying = true) }

        viewModelScope.launch {
            engine.start(bpm) {
                // On Tick (Callback from Engine)
                triggerVisualBeat()
            }
        }
    }

    private fun stopMetronome() {
        _uiState.update { it.copy(isPlaying = false) }
        engine.stop()
    }

    private fun triggerVisualBeat() {
        // Flash the indicator
        _uiState.update { it.copy(beatProgress = 1f) }

        // Quickly decay the visual pulse (purely UI effect)
        viewModelScope.launch {
            delay(100)
            _uiState.update { it.copy(beatProgress = 0f) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.stop()
    }
}