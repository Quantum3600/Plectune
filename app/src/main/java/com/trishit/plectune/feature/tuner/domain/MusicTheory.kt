package com.trishit.plectune.feature.tuner.domain

import kotlin.math.ln
import kotlin.math.roundToInt

enum class GuitarString(
    val noteName: String,
    val frequency: Float,
    val label: String
) {
    E2("E2", 82.41f, "E"),
    A2("A2", 110.00f, "A"),
    D3("D3", 146.83f, "D"),
    G3("G3", 196.00f, "G"),
    B3("B3", 246.94f, "B"),
    E4("E4", 329.63f, "E")
}

data class NoteResult(
    val name: String,
    val differenceHz: Float,
    val cents: Float
)

object MusicTheory {
    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    fun getNote(frequency: Float, targetHz: Float? = null): NoteResult {
        if(targetHz != null) {
            val diff = frequency - targetHz
            val cents = 1200 * (ln(frequency / targetHz.toDouble()) / ln(2.0)).toFloat()
            return NoteResult("Target", diff, cents)
        }
        val midiNumber = (69 + 12 * ln(frequency / 440.0) / ln(2.0)).toFloat()
        val nearestMidi = midiNumber.roundToInt()
        val differenceInCents = (midiNumber - nearestMidi) * 100
        val noteIndex = nearestMidi % 12
        val octave = (nearestMidi / 12) - 1
        val name = "${noteNames[noteIndex]}$octave"
        return NoteResult(name, differenceInCents, differenceInCents)
    }
}