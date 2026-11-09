package com.roraxos.linuxenv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentGreen = Color(0xFF00E676)
val AccentGreenDim = Color(0xFF1B5E20)
val BackgroundDark = Color(0xFF0A0E0C)
val SurfaceDark = Color(0xFF141917)
val SurfaceDarkElevated = Color(0xFF1C2320)
val TextPrimary = Color(0xFFE6F2EC)
val TextSecondary = Color(0xFF8FA69B)
val ErrorRed = Color(0xFFFF5252)
val WarningAmber = Color(0xFFFFB300)

private val RoraxDarkScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    secondary = AccentGreenDim,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

@Composable
fun RoraxOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RoraxDarkScheme,
        typography = RoraxTypography,
        content = content
    )
}
