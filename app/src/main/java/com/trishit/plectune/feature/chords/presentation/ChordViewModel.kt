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
                val chords = ChordRepository.getChordsByRoot(event.root)
                _uiState.update {
                    it.copy(
                        selectedRoot = event.root,
                        displayedChords = chords,
                        selectedVariantIndex = 0 // Reset to first variant when root changes
                    )
                }
            }
            is ChordEvent.SelectVariant -> {
                _uiState.update { it.copy(selectedVariantIndex = event.index) }
            }
            is ChordEvent.PlayChord -> {
                chordPlayer.playChord(event.chord)
            }
            ChordEvent.PlaySelected -> {
                _uiState.value.selectedChord?.let { chordPlayer.playChord(it) }
            }
        }
    }

    private fun loadChords(root: String) {
        val chords = ChordRepository.getChordsByRoot(root)
        _uiState.update {
            it.copy(
                displayedChords = chords,
                selectedVariantIndex = it.selectedVariantIndex.coerceIn(0, (chords.size - 1).coerceAtLeast(0)),
            )
        }
    }
}
