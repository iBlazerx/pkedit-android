package com.pkedit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF2D1B00),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFB39DDB),
    tertiary = Color(0xFF80CBC4),
    background = Color(0xFF101010),
    surface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurface = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFFB0B0B0),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFE65100),
    secondary = Color(0xFF5E35B1),
    tertiary = Color(0xFF00897B),
)

@Composable
fun PkEditTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
