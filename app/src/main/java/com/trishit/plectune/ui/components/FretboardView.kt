package com.trishit.plectune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.trishit.plectune.feature.chords.domain.Chord

@Composable
fun FretboardView(
    modifier: Modifier,
    chord: Chord
) {
    Canvas(modifier = Modifier.size(120.dp, 160.dp)) {
        val w = size.width
        val h = size.height
        val stringSpacing = w / 5
        val fretSpacing = h / 5

        // Draw Frets (Horizontal)
        for (i in 0..5) {
            val y = i * fretSpacing
            drawLine(Color.DarkGray, Offset(0f, y), Offset(w, y), strokeWidth = 3f)
        }

        // Draw Strings (Vertical)
        for (i in 0..5) {
            val x = i * stringSpacing
            drawLine(Color.Gray, Offset(x, 0f), Offset(x, h), strokeWidth = 2f + (i * 0.5f))
        }

        // Draw Dots
        chord.frets.forEachIndexed { stringIndex, fret ->
            val x = stringIndex * stringSpacing

            if (fret > 0) {
                val y = (fret * fretSpacing) - (fretSpacing / 2)
                drawCircle(Color(0xFF32D74B), 14f, Offset(x, y))
            } else if (fret == -1) {
                // X (Muted)
                val topY = 10f
                drawLine(Color.Red, Offset(x - 8, topY), Offset(x + 8, topY + 16), strokeWidth = 3f)
                drawLine(Color.Red, Offset(x + 8, topY), Offset(x - 8, topY + 16), strokeWidth = 3f)
            } else {
                // O (Open)
                drawCircle(Color.White, 8f, Offset(x, 18f), style = Stroke(2f))
            }
        }
    }
}