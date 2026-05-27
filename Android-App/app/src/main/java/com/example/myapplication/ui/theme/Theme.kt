package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EparkColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = MintAccent,
    onPrimaryContainer = TextPrimary,
    secondary = MintAccent,
    onSecondary = TextPrimary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = AppBackground,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = BorderColor,
)

@Composable
fun EparkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EparkColorScheme,
        typography = EparkTypography,
        content = content,
    )
}
