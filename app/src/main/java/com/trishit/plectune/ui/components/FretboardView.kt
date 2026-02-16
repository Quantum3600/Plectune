package com.trishit.plectune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.trishit.plectune.feature.chords.domain.Chord
import com.trishit.plectune.ui.theme.PlectuneGreen

@Composable
fun FretboardView(
    modifier: Modifier,
    chord: Chord
) {
    Canvas(modifier = modifier.then(Modifier.size(120.dp, 160.dp))) {
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

        // ---- Barre support (requested): if consecutive 1s exist, draw a line (barre) ----
        // We interpret "consecutive 1s" as adjacent strings having fret == 1.
        run {
            val frets = chord.frets
            var i = 0
            while (i < frets.size) {
                if (frets[i] == 1) {
                    val start = i
                    var end = i
                    while (end + 1 < frets.size && frets[end + 1] == 1) end++

                    if (end > start) {
                        val y = (1 * fretSpacing) - (fretSpacing / 2)
                        val xStart = start * stringSpacing
                        val xEnd = end * stringSpacing

                        drawLine(
                            color = PlectuneGreen,
                            start = Offset(xStart, y),
                            end = Offset(xEnd, y),
                            strokeWidth = 18f,
                            cap = StrokeCap.Round
                        )
                    }
                    i = end + 1
                } else {
                    i++
                }
            }
        }

        // Draw Dots / X / O
        chord.frets.forEachIndexed { stringIndex, fret ->
            val x = stringIndex * stringSpacing

            if (fret > 0) {
                val y = (fret * fretSpacing) - (fretSpacing / 2)
                drawCircle(PlectuneGreen, 14f, Offset(x, y))
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
