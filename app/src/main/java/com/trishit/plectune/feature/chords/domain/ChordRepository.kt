package com.trishit.plectune.feature.chords.domain

object ChordRepository {
    val chords = listOf(
        // C chords
        Chord("C Major", "C", "maj", listOf(-1, 3, 2, 0, 1, 0)),
        Chord("C Minor", "C", "min", listOf(-1, 3, 5, 5, 4, 3)),
        Chord("C5", "C", "5", listOf(-1, 3, 5, 5, -1, -1)),
        Chord("C7", "C", "7", listOf(-1, 3, 2, 3, 1, 0)),
        Chord("Cmaj7", "C", "maj7", listOf(-1, 3, 2, 0, 0, 0)),
        Chord("Cm7", "C", "m7", listOf(-1, 3, 5, 3, 4, 3)),
        Chord("Csus2", "C", "sus2", listOf(-1, 3, 0, 0, 3, 3)),
        Chord("C7sus4", "C", "7sus4", listOf(-1, 3, 3, 3, 1, 1)),
        Chord("C7#9", "C", "7#9", listOf(-1, 3, 2, 3, 3, 4)),
        Chord("C9", "C", "9", listOf(-1, 3, 2, 3, 3, 3)),

        // C# / Db chords
        Chord("C# Major", "C#", "maj", listOf(-1, 4, 3, 1, 2, 1)),
        Chord("C# Minor", "C#", "min", listOf(-1, 4, 6, 6, 5, 4)),
        Chord("C#5", "C#", "5", listOf(-1, 4, 6, 6, -1, -1)),
        Chord("C#7", "C#", "7", listOf(-1, 4, 3, 4, 2, 1)),
        Chord("C#maj7", "C#", "maj7", listOf(-1, 4, 3, 1, 1, 1)),
        Chord("C#m7", "C#", "m7", listOf(-1, 4, 6, 4, 5, 4)),
        Chord("C#sus2", "C#", "sus2", listOf(-1, 4, 1, 1, 4, 4)),
        Chord("C#7sus4", "C#", "7sus4", listOf(-1, 4, 4, 4, 2, 2)),
        Chord("C#7#9", "C#", "7#9", listOf(-1, 4, 3, 4, 4, 5)),
        Chord("C#9", "C#", "9", listOf(-1, 4, 3, 4, 4, 4)),

        // D chords
        Chord("D Major", "D", "maj", listOf(-1, -1, 0, 2, 3, 2)),
        Chord("D Minor", "D", "min", listOf(-1, -1, 0, 2, 3, 1)),
        Chord("D5", "D", "5", listOf(-1, -1, 0, 2, 3, -1)),
        Chord("D7", "D", "7", listOf(-1, -1, 0, 2, 1, 2)),
        Chord("Dmaj7", "D", "maj7", listOf(-1, -1, 0, 2, 2, 2)),
        Chord("Dm7", "D", "m7", listOf(-1, -1, 0, 2, 1, 1)),
        Chord("Dsus2", "D", "sus2", listOf(-1, -1, 0, 2, 3, 0)),
        Chord("D7sus4", "D", "7sus4", listOf(-1, -1, 0, 2, 1, 3)),
        Chord("D7#9", "D", "7#9", listOf(-1, 5, 4, 5, 5, 6)),
        Chord("D9", "D", "9", listOf(-1, 5, 4, 5, 5, 5)),

        // D# / Eb chords
        Chord("D# Major", "D#", "maj", listOf(-1, -1, 1, 3, 4, 3)),
        Chord("D# Minor", "D#", "min", listOf(-1, -1, 1, 3, 4, 2)),
        Chord("D#5", "D#", "5", listOf(-1, -1, 1, 3, 4, -1)),
        Chord("D#7", "D#", "7", listOf(-1, -1, 1, 3, 2, 3)),
        Chord("D#maj7", "D#", "maj7", listOf(-1, -1, 1, 3, 3, 3)),
        Chord("D#m7", "D#", "m7", listOf(-1, -1, 1, 3, 2, 2)),
        Chord("D#sus2", "D#", "sus2", listOf(-1, -1, 1, 3, 4, 1)),
        Chord("D#7sus4", "D#", "7sus4", listOf(-1, -1, 1, 3, 2, 4)),
        Chord("D#7#9", "D#", "7#9", listOf(-1, 6, 5, 6, 6, 7)),
        Chord("D#9", "D#", "9", listOf(-1, 6, 5, 6, 6, 6)),

        // E chords
        Chord("E Major", "E", "maj", listOf(0, 2, 2, 1, 0, 0)),
        Chord("E Minor", "E", "min", listOf(0, 2, 2, 0, 0, 0)),
        Chord("E5", "E", "5", listOf(0, 2, 2, -1, -1, -1)),
        Chord("E7", "E", "7", listOf(0, 2, 0, 1, 0, 0)),
        Chord("Emaj7", "E", "maj7", listOf(0, 2, 1, 1, 0, 0)),
        Chord("Em7", "E", "m7", listOf(0, 2, 0, 0, 0, 0)),
        Chord("Esus2", "E", "sus2", listOf(0, 2, 4, 4, 0, 0)),
        Chord("E7sus4", "E", "7sus4", listOf(0, 2, 0, 2, 0, 0)),
        Chord("E7#9", "E", "7#9", listOf(0, 2, 0, 1, 3, 2)),
        Chord("E9", "E", "9", listOf(0, 2, 0, 1, 0, 2)),

        // F chords
        Chord("F Major", "F", "maj", listOf(1, 3, 3, 2, 1, 1)),
        Chord("F Minor", "F", "min", listOf(1, 3, 3, 1, 1, 1)),
        Chord("F5", "F", "5", listOf(1, 3, 3, -1, -1, -1)),
        Chord("F7", "F", "7", listOf(1, 3, 1, 2, 1, 1)),
        Chord("Fmaj7", "F", "maj7", listOf(1, 3, 2, 2, 1, 1)),
        Chord("Fm7", "F", "m7", listOf(1, 3, 1, 1, 1, 1)),
        Chord("Fsus2", "F", "sus2", listOf(1, 3, 3, 0, 1, 1)),
        Chord("F7sus4", "F", "7sus4", listOf(1, 3, 1, 3, 1, 1)),
        Chord("F7#9", "F", "7#9", listOf(1, 3, 1, 2, 4, 3)),
        Chord("F9", "F", "9", listOf(1, 3, 1, 2, 1, 3)),

        // F# / Gb chords
        Chord("F# Major", "F#", "maj", listOf(2, 4, 4, 3, 2, 2)),
        Chord("F# Minor", "F#", "min", listOf(2, 4, 4, 2, 2, 2)),
        Chord("F#5", "F#", "5", listOf(2, 4, 4, -1, -1, -1)),
        Chord("F#7", "F#", "7", listOf(2, 4, 2, 3, 2, 2)),
        Chord("F#maj7", "F#", "maj7", listOf(2, 4, 3, 3, 2, 2)),
        Chord("F#m7", "F#", "m7", listOf(2, 4, 2, 2, 2, 2)),
        Chord("F#sus2", "F#", "sus2", listOf(2, 4, 4, 1, 2, 2)),
        Chord("F#7sus4", "F#", "7sus4", listOf(2, 4, 2, 4, 2, 2)),
        Chord("F#7#9", "F#", "7#9", listOf(2, 4, 2, 3, 5, 4)),
        Chord("F#9", "F#", "9", listOf(2, 4, 2, 3, 2, 4)),

        // G chords
        Chord("G Major", "G", "maj", listOf(3, 2, 0, 0, 0, 3)),
        Chord("G Minor", "G", "min", listOf(3, 5, 5, 3, 3, 3)),
        Chord("G5", "G", "5", listOf(3, 5, 5, -1, -1, -1)),
        Chord("G7", "G", "7", listOf(3, 2, 0, 0, 0, 1)),
        Chord("Gmaj7", "G", "maj7", listOf(3, 2, 0, 0, 0, 2)),
        Chord("Gm7", "G", "m7", listOf(3, 5, 3, 3, 3, 3)),
        Chord("Gsus2", "G", "sus2", listOf(3, 0, 0, 0, 3, 3)),
        Chord("G7sus4", "G", "7sus4", listOf(3, 3, 0, 0, 1, 1)),
        Chord("G7#9", "G", "7#9", listOf(3, -1, 3, 4, 4, 5)),
        Chord("G9", "G", "9", listOf(3, 2, 0, 2, 0, 1)),

        // G# / Ab chords
        Chord("G# Major", "G#", "maj", listOf(4, 6, 6, 5, 4, 4)),
        Chord("G# Minor", "G#", "min", listOf(4, 6, 6, 4, 4, 4)),
        Chord("G#5", "G#", "5", listOf(4, 6, 6, -1, -1, -1)),
        Chord("G#7", "G#", "7", listOf(4, 6, 4, 5, 4, 4)),
        Chord("G#maj7", "G#", "maj7", listOf(4, 6, 5, 5, 4, 4)),
        Chord("G#m7", "G#", "m7", listOf(4, 6, 4, 4, 4, 4)),
        Chord("G#sus2", "G#", "sus2", listOf(4, 6, 6, 3, 4, 4)),
        Chord("G#7sus4", "G#", "7sus4", listOf(4, 6, 4, 6, 4, 4)),
        Chord("G#7#9", "G#", "7#9", listOf(4, 6, 4, 5, 7, 6)),
        Chord("G#9", "G#", "9", listOf(4, 6, 4, 5, 4, 6)),

        // A chords
        Chord("A Major", "A", "maj", listOf(-1, 0, 2, 2, 2, 0)),
        Chord("A Minor", "A", "min", listOf(-1, 0, 2, 2, 1, 0)),
        Chord("A5", "A", "5", listOf(-1, 0, 2, 2, -1, -1)),
        Chord("A7", "A", "7", listOf(-1, 0, 2, 0, 2, 0)),
        Chord("Amaj7", "A", "maj7", listOf(-1, 0, 2, 1, 2, 0)),
        Chord("Am7", "A", "m7", listOf(-1, 0, 2, 0, 1, 0)),
        Chord("Asus2", "A", "sus2", listOf(-1, 0, 2, 2, 0, 0)),
        Chord("A7sus4", "A", "7sus4", listOf(-1, 0, 2, 0, 3, 0)),
        Chord("A7#9", "A", "7#9", listOf(-1, 0, 2, 0, 2, 3)),
        Chord("A9", "A", "9", listOf(-1, 0, 2, 4, 2, 3)),

        // A# / Bb chords
        Chord("A# Major", "A#", "maj", listOf(-1, 1, 3, 3, 3, 1)),
        Chord("A# Minor", "A#", "min", listOf(-1, 1, 3, 3, 2, 1)),
        Chord("A#5", "A#", "5", listOf(-1, 1, 3, 3, -1, -1)),
        Chord("A#7", "A#", "7", listOf(-1, 1, 3, 1, 3, 1)),
        Chord("A#maj7", "A#", "maj7", listOf(-1, 1, 3, 2, 3, 1)),
        Chord("A#m7", "A#", "m7", listOf(-1, 1, 3, 1, 2, 1)),
        Chord("A#sus2", "A#", "sus2", listOf(-1, 1, 3, 3, 1, 1)),
        Chord("A#7sus4", "A#", "7sus4", listOf(-1, 1, 3, 1, 4, 1)),
        Chord("A#7#9", "A#", "7#9", listOf(-1, 1, 3, 1, 3, 4)),
        Chord("A#9", "A#", "9", listOf(-1, 1, 0, 1, 1, 1)),

        // B chords
        Chord("B Major", "B", "maj", listOf(-1, 2, 4, 4, 4, 2)),
        Chord("B Minor", "B", "min", listOf(-1, 2, 4, 4, 3, 2)),
        Chord("B5", "B", "5", listOf(-1, 2, 4, 4, -1, -1)),
        Chord("B7", "B", "7", listOf(-1, 2, 1, 2, 0, 2)),
        Chord("Bmaj7", "B", "maj7", listOf(-1, 2, 4, 3, 4, 2)),
        Chord("Bm7", "B", "m7", listOf(-1, 2, 4, 2, 3, 2)),
        Chord("Bsus2", "B", "sus2", listOf(-1, 2, 4, 4, 2, 2)),
        Chord("B7sus4", "B", "7sus4", listOf(-1, 2, 4, 2, 5, 2)),
        Chord("B7#9", "B", "7#9", listOf(-1, 2, 1, 2, 4, 3)),
        Chord("B9", "B", "9", listOf(-1, 2, 1, 2, 2, 2)),
    )
    fun getChordsByRoot(root: String): List<Chord> {
        return chords.filter { it.root == root }
    }
}