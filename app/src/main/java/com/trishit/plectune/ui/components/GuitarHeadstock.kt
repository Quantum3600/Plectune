package com.trishit.plectune.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.trishit.plectune.R
import com.trishit.plectune.feature.tuner.domain.GuitarString
import com.trishit.plectune.ui.theme.PlectuneGreen

@Composable
fun GuitarHeadstock(
    modifier: Modifier,
    activeString: GuitarString?,
    backdrop: Backdrop,
    onStringSelected: (GuitarString) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp), // Adjust height to fit your image
        contentAlignment = Alignment.Center
    ) {
        // 1. The Headstock Image
        Image(
            painter = painterResource(id = R.drawable.headstock), // Your image here
            contentDescription = "Guitar Headstock",
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 3.dp)
                .scale(1.2f), // Fills the box
            contentScale = ContentScale.Fit, // Keeps aspect ratio intact
            alpha = 0.8f // Slight transparency for that "dark mode" look
        )

        // 2. The Interactive Pegs (Overlay)
        // We use Alignment Bias to position them relative to the image center.
        // You might need to tweak these numbers to match your specific image!

        // Left Side (D, A, E)
        Column(
            Modifier
                .align(Alignment.CenterStart)
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PegButton(
                string = GuitarString.D3,
                activeString = activeString,
                backdrop = backdrop,
                onClick = onStringSelected
            )
            PegButton(
                string = GuitarString.A2,
                activeString = activeString,
                backdrop = backdrop,
                onClick = onStringSelected
            )
            PegButton(
                string = GuitarString.E2,
                activeString = activeString,
                backdrop = backdrop,
                onClick = onStringSelected
            )
        }

        // Right Side (G, B, e)
        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PegButton(
                string = GuitarString.G3,
                activeString = activeString,
                backdrop = backdrop,
                onClick = onStringSelected
            )
            PegButton(
                string = GuitarString.B3,
                activeString = activeString,
                backdrop = backdrop,
                onClick = onStringSelected
            )
            PegButton(
                string = GuitarString.E4,
                activeString = activeString,
                backdrop = backdrop,
                onClick = onStringSelected
            )
        }
    }
}

@Composable
fun PegButton(
    string: GuitarString,
    activeString: GuitarString?,
    backdrop: Backdrop,
    onClick: (GuitarString) -> Unit,
) {
    val isActive = activeString == string

    LiquidButton(
        onClick = { onClick(string) },
        backdrop = backdrop,
        modifier = Modifier
            .size(64.dp)
            .padding(4.dp),
        surfaceColor = if (isActive) PlectuneGreen.copy(alpha = 0.25f) else Color.Transparent,
        content = {
            Text(
                text = string.label,
                color = if (isActive) PlectuneGreen else MaterialTheme.colorScheme.onBackground,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
    )
}
@Preview
@Composable
fun GuitarHeadstockPreview() {
    GuitarHeadstock(
        Modifier,
        activeString = GuitarString.E2,
        backdrop = rememberLayerBackdrop(),
        onStringSelected = {},
    )
}
