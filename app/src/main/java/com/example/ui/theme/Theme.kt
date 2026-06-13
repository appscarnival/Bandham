package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GeometricPrimary,
    secondary = GeometricSecondary,
    background = GeometricBackground,
    surface = GeometricSurface,
    onPrimary = GeometricOnPrimary,
    onSecondary = GeometricOnSecondary,
    onBackground = GeometricOnBackground,
    onSurface = GeometricOnSurface,
    error = GeometricRedAccent,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    outline = GeometricOutline
)

private val LightColorScheme = lightColorScheme(
    primary = GeometricPrimary,
    secondary = GeometricSecondary,
    background = GeometricBackground,
    surface = GeometricSurface,
    onPrimary = GeometricOnPrimary,
    onSecondary = GeometricOnSecondary,
    onBackground = GeometricOnBackground,
    onSurface = GeometricOnSurface,
    error = GeometricRedAccent,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    outline = GeometricOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Emphasize our crafted relationship palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
