package com.example.bustrackerpassenger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Blue600,
    onPrimary          = White,
    primaryContainer   = Blue100,
    onPrimaryContainer = Blue700,

    secondary          = Slate500,
    onSecondary        = White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate700,

    tertiary           = Green500,
    onTertiary         = White,
    tertiaryContainer  = Green50,
    onTertiaryContainer = Green700,

    background         = Slate50,
    onBackground       = Slate900,

    surface            = White,
    onSurface          = Slate800,
    surfaceVariant     = Slate100,
    onSurfaceVariant   = Slate500,

    outline            = Slate200,
    outlineVariant     = Slate300,

    error              = Red500,
    onError            = White,
    errorContainer     = Red50,
    onErrorContainer   = Red600,
)

@Composable
fun BusTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}