package com.trishit.plectune.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.plectune.R
import com.trishit.plectune.feature.tuner.domain.GuitarString

@Composable
fun GuitarHeadstock(
    modifier: Modifier,
    activeString: GuitarString?,
    onStringSelected: (GuitarString) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp), // Adjust height to fit your image
        contentAlignment = Alignment.Center
    ) {
        // 1. The Headstock Image
        Image(
            painter = painterResource(id = R.drawable.headstock), // Your image here
            contentDescription = "Guitar Headstock",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit, // Keeps aspect ratio intact
            alpha = 0.8f // Slight transparency for that "dark mode" look
        )

        // 2. The Interactive Pegs (Overlay)
        // We use Alignment Bias to position them relative to the image center.
        // You might need to tweak these numbers to match your specific image!

        // Left Side (D, A, E)
        PegButton(
            string = GuitarString.D3,
            activeString = activeString,
            onClick = onStringSelected,
            align = BiasAlignment(-0.8f, -0.6f) // Top Left
        )
        PegButton(
            string = GuitarString.A2,
            activeString = activeString,
            onClick = onStringSelected,
            align = BiasAlignment(-0.8f, 0.0f)  // Middle Left
        )
        PegButton(
            string = GuitarString.E2,
            activeString = activeString,
            onClick = onStringSelected,
            align = BiasAlignment(-0.8f, 0.6f)  // Bottom Left
        )

        // Right Side (G, B, e)
        PegButton(
            string = GuitarString.G3,
            activeString = activeString,
            onClick = onStringSelected,
            align = BiasAlignment(0.8f, -0.6f) // Top Right
        )
        PegButton(
            string = GuitarString.B3,
            activeString = activeString,
            onClick = onStringSelected,
            align = BiasAlignment(0.8f, 0.0f)  // Middle Right
        )
        PegButton(
            string = GuitarString.E4,
            activeString = activeString,
            onClick = onStringSelected,
            align = BiasAlignment(0.8f, 0.6f)  // Bottom Right
        )
    }
}

@Composable
fun BoxScope.PegButton(
    string: GuitarString,
    activeString: GuitarString?,
    onClick: (GuitarString) -> Unit,
    align: Alignment
) {
    val isActive = activeString == string

    // Animate the Glow
    val glowColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF32D74B) else Color.Transparent,
        animationSpec = tween(300),
        label = "glow"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF32D74B) else Color.Gray.copy(alpha = 0.5f),
        label = "border"
    )

    Box(
        modifier = Modifier
            .align(align)
            .size(48.dp)
            .shadow(
                elevation = if (isActive) 15.dp else 0.dp,
                spotColor = glowColor,
                shape = CircleShape
            )
            .clip(CircleShape)
            // Glassmorphism background for the button
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
            .border(2.dp, borderColor, CircleShape)
            .clickable { onClick(string) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = string.label,
            color = if (isActive) Color.White else Color.LightGray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}