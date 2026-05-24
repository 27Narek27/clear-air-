package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = OnDarkPrimary,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force the stunning custom dark cyber-green palette across the entire app
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
