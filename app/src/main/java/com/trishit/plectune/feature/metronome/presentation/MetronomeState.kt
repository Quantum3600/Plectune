package com.trishit.plectune.feature.metronome.presentation

data class MetronomeUiState(
    val isPlaying: Boolean = false,
    val bpm: Int = 100,
    val beatProgress: Float = 0f
)

sealed class MetronomeEvent {
    object TogglePlay : MetronomeEvent()
    data class SetBpm(val bpm: Int) : MetronomeEvent()
    data class AdjustBpm(val amount: Int) : MetronomeEvent()
}