package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RadioAmberPrimary,
    onPrimary = Color(0xFF1E1400),
    primaryContainer = Color(0xFF452A00),
    onPrimaryContainer = RadioAmberLight,
    secondary = RadioCyanSecondary,
    onSecondary = Color(0xFF002F48),
    secondaryContainer = Color(0xFF00486D),
    onSecondaryContainer = Color(0xFFBCE9FF),
    tertiary = RadioGreenSignal,
    onTertiary = Color(0xFF00391F),
    tertiaryContainer = Color(0xFF00532F),
    onTertiaryContainer = Color(0xFF86F8B6),
    background = RadioDarkBg,
    onBackground = RadioTextPrimary,
    surface = RadioDarkSurface,
    onSurface = RadioTextPrimary,
    surfaceVariant = RadioDarkSurfaceVariant,
    onSurfaceVariant = RadioTextSecondary,
    outline = RadioDarkBorder,
    error = RadioRedAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark cockpit theme preferred by Ham operators
    dynamicColor: Boolean = false, // Keep consistent high-contrast radio colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
