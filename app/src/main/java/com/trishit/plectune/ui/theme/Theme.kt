package com.trishit.plectune.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = LightBackground,
    surface = LightBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
)

@Composable
fun PlectuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AnimatedPlectuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    animationDuration: Int = 600,
    content: @Composable () -> Unit
) {
    val targetScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Animate all colors
    val primary by animateColorAsState(targetScheme.primary, tween(animationDuration))
    val onPrimary by animateColorAsState(targetScheme.onPrimary, tween(animationDuration))
    val primaryContainer by animateColorAsState(targetScheme.primaryContainer, tween(animationDuration))
    val onPrimaryContainer by animateColorAsState(targetScheme.onPrimaryContainer, tween(animationDuration))
    val inversePrimary by animateColorAsState(targetScheme.inversePrimary, tween(animationDuration))
    val secondary by animateColorAsState(targetScheme.secondary, tween(animationDuration))
    val onSecondary by animateColorAsState(targetScheme.onSecondary, tween(animationDuration))
    val secondaryContainer by animateColorAsState(targetScheme.secondaryContainer, tween(animationDuration))
    val onSecondaryContainer by animateColorAsState(targetScheme.onSecondaryContainer, tween(animationDuration))
    val tertiary by animateColorAsState(targetScheme.tertiary, tween(animationDuration))
    val onTertiary by animateColorAsState(targetScheme.onTertiary, tween(animationDuration))
    val tertiaryContainer by animateColorAsState(targetScheme.tertiaryContainer, tween(animationDuration))
    val onTertiaryContainer by animateColorAsState(targetScheme.onTertiaryContainer, tween(animationDuration))
    val background by animateColorAsState(targetScheme.background, tween(animationDuration))
    val onBackground by animateColorAsState(targetScheme.onBackground, tween(animationDuration))
    val surface by animateColorAsState(targetScheme.surface, tween(animationDuration))
    val onSurface by animateColorAsState(targetScheme.onSurface, tween(animationDuration))
    val surfaceVariant by animateColorAsState(targetScheme.surfaceVariant, tween(animationDuration))
    val onSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, tween(animationDuration))
    val surfaceTint by animateColorAsState(targetScheme.surfaceTint, tween(animationDuration))
    val inverseSurface by animateColorAsState(targetScheme.inverseSurface, tween(animationDuration))
    val inverseOnSurface by animateColorAsState(targetScheme.inverseOnSurface, tween(animationDuration))
    val error by animateColorAsState(targetScheme.error, tween(animationDuration))
    val onError by animateColorAsState(targetScheme.onError, tween(animationDuration))
    val errorContainer by animateColorAsState(targetScheme.errorContainer, tween(animationDuration))
    val onErrorContainer by animateColorAsState(targetScheme.onErrorContainer, tween(animationDuration))
    val outline by animateColorAsState(targetScheme.outline, tween(animationDuration))
    val outlineVariant by animateColorAsState(targetScheme.outlineVariant, tween(animationDuration))
    val scrim by animateColorAsState(targetScheme.scrim, tween(animationDuration))

    val animatedColorScheme = ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}
