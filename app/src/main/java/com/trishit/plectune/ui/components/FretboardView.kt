package com.trishit.plectune.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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

        // Decide which 5-fret "window" to render so chords don't go out of the board.
        val fretted = chord.frets.filter { it > 0 }
        val minFret = fretted.minOrNull() ?: 1
        val maxFret = fretted.maxOrNull() ?: 1
        val baseFret = if (maxFret <= 5) 1 else minFret.coerceAtLeast(1)

        fun yForFret(fret: Int): Float {
            // Fret numbers on screen are: baseFret .. baseFret+4
            // Convert absolute fret -> position within the 5 visible frets.
            val relative = (fret - baseFret + 1).coerceIn(1, 5)
            return (relative * fretSpacing) - (fretSpacing / 2)
        }

        fun sanitizeFingerValue(v: Int?): Int {
            // Valid: 1..5 (Index..Thumb). Anything else means "no finger color".
            return (v ?: 0).takeIf { it in 1..5 } ?: 0
        }

        fun fingerColorForString(stringIndex: Int): Color {
            val raw = chord.fingers?.getOrNull(stringIndex)
            val finger = sanitizeFingerValue(raw)
            return if (finger == 0) PlectuneGreen else getFingerColor(finger)
        }

        fun isStringCoveredByBarre(stringIndex: Int): Boolean {
            if (!chord.isBarre) return false
            val start = chord.barreStartString ?: return false
            val end = chord.barreEndString ?: return false
            val s = minOf(start, end)
            val e = maxOf(start, end)
            return stringIndex in s..e
        }

        // Draw Frets (Horizontal)
        for (i in 0..5) {
            val y = i * fretSpacing
            drawLine(Color.DarkGray, Offset(0f, y), Offset(w, y), strokeWidth = 5f)
        }

        // Draw Strings (Vertical)
        for (i in 0..5) {
            val x = i * stringSpacing
            drawLine(Color.Gray, Offset(x, 0f), Offset(x, h), strokeWidth = 5f - (i * 0.5f))
        }

        // Draw Fret Numbers (match the baseFret window)
        val textPaint = Paint().apply {
            color = Color.Gray.toArgb()
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
        }

        drawIntoCanvas { canvas ->
            for (i in 1..5) {
                val y = (i * fretSpacing) - (fretSpacing / 2)
                canvas.nativeCanvas.drawText(
                    (baseFret + i - 1).toString(),
                    -8f,
                    y + 8f,
                    textPaint
                )
            }
        }

        // Draw Barre (uses baseFret window too)
        if (chord.isBarre) {
            val start = chord.barreStartString
            val end = chord.barreEndString
            val barreFret = chord.barreFret
            if (start != null && end != null && barreFret != null) {
                val safeStart = minOf(start, end).coerceIn(0, 5)
                val safeEnd = maxOf(start, end).coerceIn(0, 5)

                // If the barre fret is outside the visible window, clamp it so it stays on-board.
                val y = yForFret(barreFret)
                val xStart = safeStart * stringSpacing
                val xEnd = safeEnd * stringSpacing

                val barreFinger = sanitizeFingerValue(chord.barreFinger)
                val barreColor = if (barreFinger == 0) PlectuneGreen else getFingerColor(barreFinger)

                drawLine(
                    color = barreColor,
                    start = Offset(xStart, y),
                    end = Offset(xEnd, y),
                    strokeWidth = 18f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw Dots / X / O with finger colors
        chord.frets.forEachIndexed { stringIndex, fret ->
            val x = stringIndex * stringSpacing

            if (fret > 0) {
                // If this string is covered by the barre at the same fret, don't draw a duplicate dot.
                val coveredByBarre =
                    chord.isBarre &&
                            isStringCoveredByBarre(stringIndex) &&
                            chord.barreFret != null &&
                            fret == chord.barreFret

                if (!coveredByBarre) {
                    val y = yForFret(fret)
                    val fingerColor = fingerColorForString(stringIndex)
                    drawCircle(fingerColor, 14f, Offset(x, y))
                }
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