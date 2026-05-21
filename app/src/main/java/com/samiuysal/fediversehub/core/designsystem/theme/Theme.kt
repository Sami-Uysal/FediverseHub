package com.samiuysal.fediversehub.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = HubBlue,
    onPrimary = HubSurface,
    secondary = HubGreen,
    tertiary = HubRose,
    background = HubCanvas,
    onBackground = HubInk,
    surface = HubSurface,
    onSurface = HubInk,
    surfaceVariant = HubSurfaceElevated,
    onSurfaceVariant = HubInkMuted,
    outline = HubLine,
    error = HubRose,
)

private val DarkColorScheme = darkColorScheme(
    primary = HubBlueDark,
    onPrimary = HubInk,
    secondary = ColorTokens.darkGreen,
    tertiary = ColorTokens.darkRose,
    background = HubDarkCanvas,
    onBackground = HubSurface,
    surface = HubDarkSurface,
    onSurface = HubSurface,
    surfaceVariant = HubDarkSurfaceElevated,
    onSurfaceVariant = ColorTokens.darkMuted,
    outline = HubDarkLine,
    error = ColorTokens.darkRose,
)

private object ColorTokens {
    val darkGreen = androidx.compose.ui.graphics.Color(0xFF7BE0B3)
    val darkRose = androidx.compose.ui.graphics.Color(0xFFFF9BAD)
    val darkMuted = androidx.compose.ui.graphics.Color(0xFFB8C1CC)
}

@Composable
fun FediverseHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
