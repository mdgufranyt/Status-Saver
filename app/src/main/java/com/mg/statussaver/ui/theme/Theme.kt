package com.mg.statussaver.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    secondary = GreyNormalDark,
    tertiary = TealLightDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealOnContainerDark,
    surface = GreyDarkDark,
    onSurface = GreyLight,
    background = GreyDarkDark,
    onBackground = GreyLight,
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = GreyNormal,
    tertiary = TealLight,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    surface = GreyLight,
    onSurface = GreyDarkDark,
    background = Color.White,
    onBackground = GreyDarkDark,
    onPrimary = Color.Black

)

@Composable
fun StatusSaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use custom brand colors
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color based on theme
            val statusBarColor = if (darkTheme) StatusBarColorDark else StatusBarColor
            window.statusBarColor = statusBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoppinsTypography,
        content = content
    )
}