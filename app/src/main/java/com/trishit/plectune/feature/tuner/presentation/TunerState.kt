package com.trishit.plectune.feature.tuner.presentation

import com.trishit.plectune.feature.tuner.domain.GuitarString

data class TunerUiState(
    val mode: TunerMode = TunerMode.Auto,
    val noteName: String = "--",
    val currentFreq: Float = 0f,
    val centsOff: Float = 0f,
    val isStable: Boolean = false,
    val activeString: GuitarString? = null,
    val pitchHistory: List<Float> = emptyList(),
    val autoTarget: GuitarString = GuitarString.E2
)

sealed class TunerMode {
    object Auto : TunerMode()
    data class Manual(val string: GuitarString) : TunerMode()
}

sealed class TunerEvent {
    data class SelectString(val string: GuitarString) : TunerEvent()
    data class SetAutoMode(val enabled: Boolean) : TunerEvent()

    object StartListening : TunerEvent()
    object StopListening : TunerEvent()
}