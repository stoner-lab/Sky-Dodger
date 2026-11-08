package com.duy.skydodger.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SkyDodgerColorScheme = darkColorScheme(
    primary = Color(0xFFFFD166),
    onPrimary = Color(0xFF14213D),
    secondary = Color(0xFF2E8BC0),
    onSecondary = Color(0xFF0B1E3A),
    background = Color(0xFF0B1E3A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF14213D),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1F3A5F),
    onSurfaceVariant = Color(0xFFC7D7EA),
)

@Composable
fun SkyDodgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SkyDodgerColorScheme,
        content = content,
    )
}
