package com.trishit.plectune.feature.chords.domain

data class Chord(
    val name: String,
    val root: String,
    val suffix: String,
    val frets: List<Int>,
    // -1 for Muted, 0 for open, consecutive same for barre
)
