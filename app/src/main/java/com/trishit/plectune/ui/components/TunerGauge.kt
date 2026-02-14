package com.trishit.plectune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerGauge(
    cents: Float,
    history: List<Float>,
    isStable: Boolean
) {
    val activeColor = if (isStable) Color(0xFF32D74B) else Color(0xFFFF453A)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.width / 2 - 20f

        // Draw Scale
        drawArc(
            color = Color(0xFF2C2C2C),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(4f),
            topLeft = Offset(20f, 20f),
            size = Size(size.width - 40f, size.height - 40f)
        )

        // Draw Living Graph (Snake Trail)
        if (history.isNotEmpty()) {
            val path = Path()
            val points = history.size

            // Start at center (Oldest data)
            path.moveTo(centerX, centerY)

            for (i in (points - 1) downTo 0) {
                val timeFraction = 1f - (i.toFloat() / points)
                val currentRadius = maxRadius * timeFraction
                val angleDeg = 270f + history[i].coerceIn(-90f, 90f)
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val x = centerX + currentRadius * cos(angleRad).toFloat()
                val y = centerY + currentRadius * sin(angleRad).toFloat()
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, activeColor),
                    center = Offset(centerX, centerY),
                    radius = maxRadius
                ),
                style = Stroke(8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Tip
            val tipAngle = 270f + cents.coerceIn(-90f, 90f)
            val tipRad = Math.toRadians(tipAngle.toDouble())
            val tipX = centerX + maxRadius * cos(tipRad).toFloat()
            val tipY = centerY + maxRadius * sin(tipRad).toFloat()

            drawCircle(activeColor, 12f, Offset(tipX, tipY))
            if(isStable) drawCircle(activeColor.copy(0.4f), 25f, Offset(tipX, tipY))
        }
    }
}