package com.funkin.playtest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FnfCyan,
    onPrimary = Color.Black,
    secondary = FnfPink,
    onSecondary = Color.White,
    tertiary = FnfYellow,
    background = FnfDarkBg,
    onBackground = Color.White,
    surface = FnfSurface,
    onSurface = Color.White,
    surfaceVariant = FnfSurfaceLight,
    onSurfaceVariant = Color.White
)

@Composable
fun FunkinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
