package com.partitionsoft.bookshelf.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = DarkBackground,
    secondary = BrandSecondary,
    onSecondary = DarkBackground,
    tertiary = BrandTertiary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOnSurfaceVariant.copy(alpha = 0.7f)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = LightSurface,
    secondary = BrandSecondary,
    onSecondary = LightSurface,
    tertiary = BrandTertiary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOnSurfaceVariant.copy(alpha = 0.7f)
)

@Composable
fun BookShelfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val m3ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Temporary bridge for legacy Material 2 composables while screens are migrated.
    val m2Colors = if (darkTheme) {
        darkColors(
            primary = m3ColorScheme.primary,
            primaryVariant = m3ColorScheme.primary,
            secondary = m3ColorScheme.secondary,
            background = m3ColorScheme.background,
            surface = m3ColorScheme.surface,
            onPrimary = m3ColorScheme.onPrimary,
            onSecondary = m3ColorScheme.onSecondary,
            onBackground = m3ColorScheme.onBackground,
            onSurface = m3ColorScheme.onSurface
        )
    } else {
        lightColors(
            primary = m3ColorScheme.primary,
            primaryVariant = m3ColorScheme.primary,
            secondary = m3ColorScheme.secondary,
            background = m3ColorScheme.background,
            surface = m3ColorScheme.surface,
            onPrimary = m3ColorScheme.onPrimary,
            onSecondary = m3ColorScheme.onSecondary,
            onBackground = m3ColorScheme.onBackground,
            onSurface = m3ColorScheme.onSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = m3ColorScheme.surface.toArgb()
            window.navigationBarColor = m3ColorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    Material3Theme(
        colorScheme = m3ColorScheme,
        typography = InkwellTypography,
        shapes = InkwellShapes
    ) {
        CompositionLocalProvider(LocalSpacing provides Spacing()) {
            Material2Theme(
                colors = m2Colors,
                typography = Typography,
                shapes = Shapes,
                content = content
            )
        }
    }
}