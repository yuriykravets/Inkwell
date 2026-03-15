package com.partitionsoft.bookshelf.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import android.app.Activity

private val DarkColorPalette = darkColors(
    primary = BrandPrimaryDark,
    primaryVariant = BrandPrimary,
    secondary = BrandSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnSurface,
    onSecondary = DarkBackground,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

private val LightColorPalette = lightColors(
    primary = BrandPrimary,
    primaryVariant = BrandPrimaryDark,
    secondary = BrandSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightOnSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface
)

@Composable
fun BookShelfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Keep system bars readable in both dark and light mode.
            window.statusBarColor = colors.surface.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}