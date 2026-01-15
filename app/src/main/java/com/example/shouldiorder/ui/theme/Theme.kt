package com.example.shouldiorder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF6F00),
    onPrimary = Color.White,
    secondary = Color(0xFFD84315),
    onSecondary = Color.White,
    tertiary = Color(0xFFFFF3E0),
    onTertiary = Color(0xFF3E2723),
    background = Color(0xFFFFF3E0),
    onBackground = Color(0xFF3E2723),
    surface = Color.White,
    onSurface = Color(0xFF3E2723)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9D3D),
    onPrimary = Color(0xFF5D2C0C),
    secondary = Color(0xFFFF9D3D),
    onSecondary = Color(0xFF5D2C0C),
    tertiary = Color(0xFF1F1F1F),
    onTertiary = Color(0xFFFFE0B2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFE0B2),
    surface = Color(0xFF1F1F1F),
    onSurface = Color(0xFFFFE0B2)
)

@Composable
fun ShouldIOrderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

