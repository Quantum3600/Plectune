package com.trishit.plectune.feature.metronome.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.trishit.plectune.ui.components.LiquidButton
import com.trishit.plectune.ui.components.LiquidSlider
import com.trishit.plectune.ui.theme.DarkGrey850
import com.trishit.plectune.ui.theme.PlectuneGreen
import com.trishit.plectune.ui.theme.PlectuneTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel = viewModel()
) {
    val backdrop = rememberLayerBackdrop()
    val state by viewModel.uiState.collectAsState()

    val pulse = remember { Animatable(0f) }

    LaunchedEffect(state.tickId) {
        // quick “thump” in, then ease out
        val strength = if (state.isAccentedBeat) 1.15f else 0.85f
        pulse.snapTo(0f)
        pulse.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing)
        )
        pulse.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${state.bpm} BPM",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(30.dp))

        // 1. The Pulse Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .drawBehind {
                    val baseRadius = size.minDimension / 2f
                    val strokeWidthPx = 4.dp.toPx()
                    val maxWaveAmplitudePx = 10.dp.toPx() // Max deviation from base radius
                    val currentWaveAmplitude = maxWaveAmplitudePx * pulse.value

                    val path = Path().apply {
                        val numSegments = 140 // Number of points to approximate the circle
                        val numWaves = 12f // Number of waves around the circle

                        // Start the path at an initial point on the circumference
                        val startAngle = 0f
                        val startRadius =
                            baseRadius + currentWaveAmplitude * sin(numWaves * startAngle)
                        moveTo(
                            center.x + startRadius * cos(startAngle),
                            center.y + startRadius * sin(startAngle)
                        )

                        // Generate points for the wavy circle
                        for (i in 0..numSegments) {
                            val angle = (i * 2f * PI / numSegments).toFloat()
                            val waveOffset =
                                currentWaveAmplitude * sin(numWaves * angle)
                            val currentRadius = baseRadius + waveOffset

                            val x = center.x + currentRadius * cos(angle)
                            val y = center.y + currentRadius * sin(angle)
                            lineTo(x, y)
                        }
                        close()
                    }

                    // Draw the border of the circle/wave
                    drawPath(
                        path,
                        color = PlectuneGreen,
                        style = Stroke(width = strokeWidthPx)
                    )
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidButton(
                    backdrop = backdrop,
                    isInteractive = true,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(80.dp),
                    surfaceColor = PlectuneGreen.copy(alpha = 0.3f),
                    onClick = {
                        viewModel.onEvent(MetronomeEvent.BeginBpmInteraction)
                        viewModel.onEvent(MetronomeEvent.TapTempo)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Play/Stop",
                        tint = PlectuneGreen,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                LiquidButton(
                    backdrop = backdrop,
                    isInteractive = true,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(80.dp),
                    surfaceColor = PlectuneGreen.copy(alpha = 0.3f),
                    onClick = { viewModel.onEvent(MetronomeEvent.TogglePlay) },
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Play/Stop",
                        tint = PlectuneGreen,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val beats = state.timeSignature.beatsPerBar.coerceIn(1, 12)
            repeat(beats) { i ->
                val isDownBeat = i == 0
                val isActive = state.isPlaying && i == state.beatInBar

                val size = if (isDownBeat) 12.dp else 8.dp
                val color = when {
                    isActive -> PlectuneGreen
                    else -> Color.White.copy(alpha = 0.35f)
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(size)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Minus Button
            LiquidButton(
                onClick = { viewModel.onEvent(MetronomeEvent.AdjustBpm(-1)) },
                isInteractive = true,
                backdrop = backdrop,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Remove, "Decrease", tint = Color.White)
            }

            // Slider
            LiquidSlider(
                value = { state.bpm.toFloat() },
                backdrop = backdrop,
                visibilityThreshold = 0.01f,
                onValueChange = { viewModel.onEvent(MetronomeEvent.SetBpm(it.toInt())) },
                onInteractionStart = { viewModel.onEvent(MetronomeEvent.BeginBpmInteraction) },
                valueRange = 30f..240f,
                colors = SliderDefaults.colors(
                    activeTrackColor = PlectuneGreen,
                    inactiveTrackColor = DarkGrey850
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            // Plus Button
            LiquidButton(
                onClick = { viewModel.onEvent(MetronomeEvent.AdjustBpm(1)) },
                isInteractive = true,
                backdrop = backdrop,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Add, "Increase", tint = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultTimeSignatures.forEach { sig ->
                val selected = sig.beatsPerBar == state.timeSignature.beatsPerBar
                LiquidButton(
                    backdrop = backdrop,
                    isInteractive = true,
                    modifier = Modifier.size(width = 64.dp, height = 40.dp),
                    surfaceColor = if (selected) PlectuneGreen.copy(alpha = 0.25f) else DarkGrey850,
                    onClick = { viewModel.onEvent(MetronomeEvent.SetTimeSignature(sig)) }
                ) {
                    Text(
                        text = sig.label,
                        color = if (selected) PlectuneGreen else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MetronomeScreenPreview() {
    PlectuneTheme {
        MetronomeScreen()
    }
}
