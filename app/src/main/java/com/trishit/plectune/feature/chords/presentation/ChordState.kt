package com.trishit.plectune.feature.chords.presentation

import com.trishit.plectune.feature.chords.domain.Chord

data class ChordUiState(
    val selectedRoot: String = "C",
    val availableRoots: List<String> = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"),
    val displayedChords: List<Chord> = emptyList(),
    val selectedVariantIndex: Int = 0,
) {
    val selectedChord: Chord?
        get() = displayedChords.getOrNull(selectedVariantIndex)
}

sealed class ChordEvent {
    data class SelectRoot(val root: String) : ChordEvent()
    data class SelectVariant(val index: Int) : ChordEvent()
    data class PlayChord(val chord: Chord) : ChordEvent()
    data object PlaySelected : ChordEvent()
}
