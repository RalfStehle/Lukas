package de.project.lukas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RoyalBlue = Color(0xFF4169E1)
private val SteelBlue = Color(0xFF4682B4)
private val Coral = Color(0xFFFF7043)

// Background behind the device/remote lists (matches the original #eceff1).
val ListBackground = Color(0xFFECEFF1)

// Button palette, taken verbatim from the original layouts.
val MotorBlue = Color(0xFF03A9F4)
val MotorBlueDark = Color(0xFF0288D1)
val LightOrange = Color(0xFFFFA726)
val LedOrange = Color(0xFFEF6C00)
val Toggle1Green = Color(0xFF8FBC8F) // DarkSeaGreen
val Toggle2Green = Color(0xFF2E8B57) // SeaGreen

// Floating action buttons.
val ScanGreen = Color(0xFF43A047)
val ScanStopRed = Color(0xFFE53935)
val StopAllRed = Color(0xFF8B0000) // DarkRed

private val LightColors = lightColorScheme(
    primary = RoyalBlue,
    secondary = Coral,
)

private val DarkColors = darkColorScheme(
    primary = SteelBlue,
    secondary = Coral,
)

@Composable
fun LukasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
