package com.trishit.plectune.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.trishit.plectune.ui.theme.PlectuneGreen
import kotlin.random.Random

@Composable
fun AppTextureBackground(
    gridSize: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val containerSize = remember { mutableStateOf(IntSize(500, 1000)) }

    Box(modifier = modifier.onGloballyPositioned { layoutCoordinates ->
        containerSize.value = layoutCoordinates.size
    }) {
        val width = containerSize.value.width.toFloat()
        val height = containerSize.value.height.toFloat()
        val gridSizePx = with(density) {
            gridSize.toPx()
        }

        val seed = (width * height).toLong()
        val random = Random(seed)

        val colorPalette = listOf(
            PlectuneGreen.copy(alpha = 0.15f),
            Color(0xFF8B4513).copy(alpha = 0.12f),
            PlectuneGreen.copy(alpha = 0.08f),
            Color(0xFF2E7D32).copy(alpha = 0.12f)
        )

        // Create a grid-based distribution
        val cellSize = gridSizePx * 1.8f // Space between grid points
        val colsCount = (width / cellSize).toInt().coerceAtLeast(1)
        val rowsCount = (height / cellSize).toInt().coerceAtLeast(1)

        for (row in 0 until rowsCount) {
            for (col in 0 until colsCount) {
                // Base grid position
                val baseX = col * cellSize + cellSize / 2
                val baseY = row * cellSize + cellSize / 2

                // Add slight random offset within the cell to avoid rigid grid look
                val offsetX = random.nextFloat() * cellSize * 0.4f - cellSize * 0.2f
                val offsetY = random.nextFloat() * cellSize * 0.4f - cellSize * 0.2f

                val posX = baseX + offsetX
                val posY = baseY + offsetY

                // Clamp to bounds
                if (posX in 0f..width && posY in 0f..height) {
                    val noteSizePx = gridSizePx * (0.6f + random.nextFloat() * 0.4f)
                    val rotation = random.nextFloat() * 360f
                    val selectedColor = colorPalette[random.nextInt(colorPalette.size)]
                    val scaleVariation = 0.8f + random.nextFloat() * 0.4f

                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Doodle music note",
                        tint = selectedColor,
                        modifier = Modifier
                            .size((noteSizePx * scaleVariation).dp)
                            .offset(
                                x = (posX - (noteSizePx * scaleVariation / 2)).dp,
                                y = (posY - (noteSizePx * scaleVariation / 2)).dp
                            )
                            .rotate(rotation)
                    )
                }
            }
        }
    }
}

