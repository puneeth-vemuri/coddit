package com.coddit.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CodditTeal,
    onPrimary = Color.White,
    primaryContainer = CodditTeal.copy(alpha = 0.1f),
    onPrimaryContainer = CodditTeal,
    secondary = BytesPurple,
    onSecondary = Color.White,
    secondaryContainer = BytesPurple.copy(alpha = 0.1f),
    onSecondaryContainer = BytesPurple,
    background = CodditDark,
    onBackground = Color.White,
    surface = CodditSurface,
    onSurface = Color.White,
    surfaceVariant = CodditCard,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outline = Color.White.copy(alpha = 0.12f),
    outlineVariant = Color.White.copy(alpha = 0.06f)
)

@Composable
fun CodditTheme(
    darkTheme: Boolean = true, // Force Dark Mode
    dynamicColor: Boolean = false, // Disable dynamic colors for brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
