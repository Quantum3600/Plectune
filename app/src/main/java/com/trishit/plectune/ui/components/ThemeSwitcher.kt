package com.trishit.plectune.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop

@Composable
fun ThemeSwitcher(
    isDark: Boolean,
    onToggle: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    LiquidButton(
        onClick = onToggle,
        isInteractive = true,
        backdrop = backdrop,
        modifier = modifier.size(48.dp)
    ) {
        Crossfade(targetState = isDark, label = "themeIcon") { dark ->
            Icon(
                imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
