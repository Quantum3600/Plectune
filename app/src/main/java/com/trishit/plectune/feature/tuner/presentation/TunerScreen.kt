package com.trishit.plectune.feature.tuner.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trishit.plectune.ui.components.GuitarHeadstock
import com.trishit.plectune.ui.components.TunerGauge
import com.trishit.plectune.ui.theme.PlectuneGreen

@Composable
fun TunerScreen(
    viewModel: TunerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(TunerEvent.StartListening)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, bottom = 20.dp), // Add padding for status bar/nav bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. The Note Display (Top) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                text = state.noteName,
                fontSize = 120.sp,
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

        Spacer(modifier = Modifier.weight(1f))

        // --- 2. The Living Gauge (Middle) ---
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            TunerGauge(
                cents = state.centsOff,
                history = state.pitchHistory,
                isStable = state.isStable
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. The Interactive Headstock (Bottom) ---
        // Easy to reach with thumbs
        GuitarHeadstock(
            modifier = Modifier.weight(1f),
            activeString = state.activeString,
            onStringSelected = { viewModel.onEvent(TunerEvent.SelectString(it)) }
        )
    }
}
