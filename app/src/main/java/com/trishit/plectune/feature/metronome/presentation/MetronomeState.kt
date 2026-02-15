package com.trishit.plectune.feature.metronome.presentation

data class TimeSignature(
    val beatsPerBar: Int,
    val label: String
)

val DefaultTimeSignatures: List<TimeSignature> = listOf(
    TimeSignature(2, "2/4"),
    TimeSignature(3, "3/4"),
    TimeSignature(4, "4/4"),
    TimeSignature(5, "5/4"),
    TimeSignature(6, "6/8"),
)

data class MetronomeUiState(
    val isPlaying: Boolean = false,
    val bpm: Int = 100,
    val beatProgress: Float = 0f,
    val tickId: Long = 0L,
    val lastTapTime: Long = 0L, // Stores the timestamp of the last tap in milliseconds
    val tapCount: Int = 0,       // How many taps have occurred in the current sequence
    val timeSignature: TimeSignature = TimeSignature(4, "4/4"),
    val beatInBar: Int = 0,          // 0-based index
    val isAccentedBeat: Boolean = true
)

sealed class MetronomeEvent {
    object TogglePlay : MetronomeEvent()
    object Stop : MetronomeEvent()
    object BeginBpmInteraction : MetronomeEvent()
    object TapTempo : MetronomeEvent()
    data class SetBpm(val bpm: Int) : MetronomeEvent()
    data class AdjustBpm(val amount: Int) : MetronomeEvent()
    data class SetTimeSignature(val signature: TimeSignature) : MetronomeEvent()
}