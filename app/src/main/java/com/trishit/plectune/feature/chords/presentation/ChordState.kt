package com.trishit.plectune.feature.chords.presentation

import com.trishit.plectune.feature.chords.domain.Chord

data class ChordUiState(
    val selectedRoot: String = "C",
    val availableRoots: List<String> = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"),
    val displayedChords: List<Chord> = emptyList()
)

sealed class ChordEvent {
    data class SelectRoot(val root: String) : ChordEvent()
    data class PlayChord(val chord: Chord) : ChordEvent()
}
