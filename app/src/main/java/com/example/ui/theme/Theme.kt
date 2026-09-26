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
    primary = PrintCyanAccent,
    onPrimary = Slate900,
    primaryContainer = PrintBlue,
    onPrimaryContainer = Color.White,
    secondary = PrintAmberLight,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = PrintAmberLight,
    tertiary = PrintEmerald,
    background = Slate900,
    surface = Slate800,
    onBackground = Slate100,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200
)

private val LightColorScheme = lightColorScheme(
    primary = PrintCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = PrintAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = PrintEmerald,
    background = Slate50,
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our intentional custom theme
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
