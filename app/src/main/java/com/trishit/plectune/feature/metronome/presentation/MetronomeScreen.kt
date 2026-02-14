package com.trishit.plectune.feature.metronome.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.trishit.plectune.ui.components.LiquidSlider

@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel = viewModel()
) {
    val backdrop = rememberLayerBackdrop()
    val state by viewModel.uiState.collectAsState()

    // Pulse Animation
    val scale by animateFloatAsState(
        targetValue = if (state.beatProgress > 0) 1.2f else 1.0f,
        animationSpec = tween(100),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. The Pulse Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .background(Color(0xFF222222), CircleShape)
                .border(4.dp, Color(0xFF32D74B), CircleShape)
                .clickable { viewModel.onEvent(MetronomeEvent.TogglePlay) }
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Play/Stop",
                tint = Color(0xFF32D74B),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(60.dp))

        // 2. BPM Controls
        Text(
            text = "${state.bpm} BPM",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(30.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Minus Button
            IconButton(
                onClick = { viewModel.onEvent(MetronomeEvent.AdjustBpm(-1)) },
                modifier = Modifier.background(Color(0xFF222222), CircleShape)
            ) {
                Icon(Icons.Default.Remove, "Decrease", tint = Color.White)
            }

            // Slider
            LiquidSlider(
                value = { state.bpm.toFloat() },
                backdrop = backdrop,
                visibilityThreshold = 0.01f,
                onValueChange = { viewModel.onEvent(MetronomeEvent.SetBpm(it.toInt())) },
                valueRange = 20f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF32D74B),
                    activeTrackColor = Color(0xFF32D74B)
                ),
                modifier = Modifier.width(300.dp).padding(horizontal = 16.dp)
            )

            // Plus Button
            IconButton(
                onClick = { viewModel.onEvent(MetronomeEvent.AdjustBpm(1)) },
                modifier = Modifier.background(Color(0xFF222222), CircleShape)
            ) {
                Icon(Icons.Default.Add, "Increase", tint = Color.White)
            }
        }
    }
}