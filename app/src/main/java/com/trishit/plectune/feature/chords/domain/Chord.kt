package com.trishit.plectune.feature.chords.domain

data class Chord(
    val name: String,
    val root: String,
    val suffix: String,
    val frets: List<Int>,
    val fingers: List<Int>? = null, // 1:Index, 2:Middle, 3:Ring, 4:Pinky, 5:Thumb
    val isBarre: Boolean = false,
    val barreStartString: Int? = null,
    val barreEndString: Int? = null,
    val barreFret: Int? = null,
    val barreFinger: Int? = 1
)
