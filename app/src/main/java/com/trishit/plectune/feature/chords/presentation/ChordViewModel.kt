package com.trishit.plectune.feature.chords.presentation

import androidx.lifecycle.ViewModel
import com.trishit.plectune.feature.chords.data.ChordPlayer
import com.trishit.plectune.feature.chords.domain.ChordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChordViewModel : ViewModel() {
    private val chordPlayer = ChordPlayer()
    private val _uiState = MutableStateFlow(ChordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load initial chords for default root "C"
        loadChords("C")
    }

    fun onEvent(event: ChordEvent) {
        when(event) {
            is ChordEvent.SelectRoot -> {
                _uiState.update { it.copy(selectedRoot = event.root) }
                loadChords(event.root)
            }
            is ChordEvent.PlayChord -> { // Add this event to your Sealed Class
                chordPlayer.playChord(event.chord)
            }
        }
    }

    private fun loadChords(root: String) {
        val chords = ChordRepository.getChordsByRoot(root)
        _uiState.update { it.copy(displayedChords = chords) }
    }
}