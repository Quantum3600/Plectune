package com.trishit.plectune.feature.tuner.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.trishit.plectune.ui.components.AppTextureBackground
import com.trishit.plectune.ui.components.GuitarHeadstock
import com.trishit.plectune.ui.components.LiquidToggle
import com.trishit.plectune.ui.components.TunerGauge
import com.trishit.plectune.ui.theme.PlectuneGreen

@Composable
fun TunerScreen(
    viewModel: TunerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val backdrop = rememberLayerBackdrop() {
        drawRect(backgroundColor)
        drawContent()
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(TunerEvent.StartListening)
    }
    
    Box(modifier = Modifier
        .fillMaxSize()
        .layerBackdrop(backdrop)) {
        AppTextureBackground(modifier = Modifier.fillMaxSize())
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp), // Add padding for status bar/nav bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 1. The Note Display (Top) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ) {
            Text(
                text = state.noteName,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.isStable) PlectuneGreen else Color.White
            )

            Text(
                text = if (state.currentFreq > 0) {
                    val sign = if (state.centsOff > 0) "+" else ""
                    "${sign}${state.centsOff.toInt()} cents"
                } else {
                    "Pluck a string"
                },
                fontSize = 18.sp,
                color = if (state.isStable) PlectuneGreen else Color.Gray
            )
        }
        Spacer(Modifier.height(32.dp))

        // --- 2. The Living Gauge (Middle) ---
        Column(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TunerGauge(
                cents = state.centsOff,
                history = state.pitchHistory,
                isStable = state.isStable,
                isIdle = state.currentFreq <= 0f
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.Start).padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Auto", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            LiquidToggle(
                selected = { state.mode is TunerMode.Auto },
                onSelect = { enabled -> viewModel.onEvent(TunerEvent.SetAutoMode(enabled)) },
                backdrop = backdrop
            )
        }
        // --- 3. The Interactive Headstock (Bottom) ---
        // Easy to reach with thumbs
        GuitarHeadstock(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.End),
            activeString = state.activeString,
            backdrop = backdrop,
            onStringSelected = { viewModel.onEvent(TunerEvent.SelectString(it)) }
        )
    }
}

@Preview
@Composable
fun TunerScreenPreview() {
    TunerScreen()
}
