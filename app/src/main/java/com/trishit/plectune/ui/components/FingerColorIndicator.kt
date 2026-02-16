package com.trishit.plectune.ui.components

import androidx.compose.ui.graphics.Color

fun getFingerColor(finger: Int?): Color {
    return when (finger) {
        1 -> Color(0xFF32D74B)   // Index - Green
        2 -> Color(0xFF00B4D8)   // Middle - Cyan
        3 -> Color(0xFFFF006E)   // Ring - Pink
        4 -> Color(0xFFFFA500)   // Pinky - Orange
        else -> Color.White       // Default
    }
}
