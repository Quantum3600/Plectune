package com.trishit.plectune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trishit.plectune.ui.theme.DarkGrey750
import com.trishit.plectune.ui.theme.PlectuneGreen
import com.trishit.plectune.ui.theme.PlectuneRed
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerGauge(
    cents: Float,
    history: List<Float>,
    isStable: Boolean,
    isIdle: Boolean
) {
    val activeColor = if (isStable) PlectuneGreen else PlectuneRed

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padding = 12f

        // Full width oval, shallow arc segment (< 180°)
        val width = size.width
        val height = size.height
        val centerX = width / 2f

        // "Horizon" chord (endpoints across the whole width)
        val leftX = padding
        val rightX = width - padding
        val chordY = height * 0.3f          // move up/down to taste
        val sagitta = height * 0.25f         // arc "height" (smaller = flatter)

        val chord = (rightX - leftX).coerceAtLeast(1f)

        // Circle radius from chord length (c) and sagitta (s):
        // r = c^2/(8s) + s/2
        val s = sagitta.coerceAtLeast(1f)
        val radius = (chord * chord) / (8f * s) + s / 2f

        // Circle center is below the chord so we see the upper arc like a horizon
        val centerY = chordY + (radius - s)

        val circleTopLeft = Offset(centerX - radius, centerY - radius)
        val circleSize = Size(radius * 2f, radius * 2f)

        // Compute start/sweep so arc goes from left endpoint to right endpoint
        fun deg(rad: Float) = (rad * 180f / Math.PI.toFloat())
        val leftAngle = deg(atan2(chordY - centerY, leftX - centerX))
        val rightAngle = deg(atan2(chordY - centerY, rightX - centerX))

        // drawArc angles: 0° at 3 o'clock, positive clockwise.
        var sweepAngle = rightAngle - leftAngle
        if (sweepAngle < 0f) sweepAngle += 360f
        val startAngle = leftAngle

        // Map cents [-50..+50] onto this arc
        val centsMax = 50f
        val fraction = ((cents.coerceIn(-centsMax, centsMax) + centsMax) / (2f * centsMax))
        val pointerAngle = startAngle + sweepAngle * fraction
        val effectivePointerAngle = if (isIdle) (startAngle + sweepAngle / 2f) else pointerAngle


        // Draw Scale
        drawArc(
            color = DarkGrey750,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 6f, cap = StrokeCap.Round),
            topLeft = circleTopLeft,
            size = circleSize
        )

        // Draw Living Graph (Snake Trail)
        if (!isIdle && history.size >= 3) {
            val points = history.size.coerceAtMost(50)
            val innerRadius = radius * 0.18f

            val trail = ArrayList<Offset>(points)
            for (i in (points - 1) downTo 0) {
                val t = 1f - (i.toFloat() / points) // newest -> ~1
                val currentRadius = innerRadius + (radius - innerRadius) * t

                val h = history[i].coerceIn(-centsMax, centsMax)
                val hf = ((h + centsMax) / (2f * centsMax))
                val angleDeg = startAngle + sweepAngle * hf
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val x = centerX + currentRadius * cos(angleRad).toFloat()
                val y = centerY + currentRadius * sin(angleRad).toFloat()
                trail.add(Offset(x, y))
            }

            val path = Path().apply {
                moveTo(trail[0].x, trail[0].y)
                for (i in 1 until trail.size - 1) {
                    val p0 = trail[i]
                    val p1 = trail[i + 1]
                    val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
                    quadraticTo(p0.x, p0.y, mid.x, mid.y)
                }
                val last = trail.last()
                lineTo(last.x, last.y)
            }

            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, activeColor),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                style = Stroke(
                    width = 10f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

            // Draw Tip
        val tipRad = Math.toRadians(effectivePointerAngle.toDouble())
        val tipX = centerX + radius * cos(tipRad).toFloat()
        val tipY = centerY + radius * sin(tipRad).toFloat()

        val tipColor = if (isIdle) DarkGrey750 else activeColor
        drawCircle(tipColor, 12f, Offset(tipX, tipY))
        if (!isIdle && isStable) drawCircle(activeColor.copy(0.4f), 25f, Offset(tipX, tipY))
    }
}
@Preview
@Composable
fun TunerGaugePreview() {
    Box(Modifier.size(400.dp)) {
        TunerGauge(cents = 0f, history = listOf(0f), isStable = true, isIdle = false)
    }
}

