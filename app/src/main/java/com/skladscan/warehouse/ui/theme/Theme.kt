package com.skladscan.warehouse.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object SkladTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val DarkScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF101505),
    primaryContainer = Color(0xFF23330A),
    onPrimaryContainer = Lime,
    secondary = CyanBright,
    onSecondary = Color(0xFF04262B),
    tertiary = AmberBright,
    onTertiary = Color(0xFF2C1D00),
    background = Ink,
    onBackground = TextLight,
    surface = Color(0xFF101722),
    onSurface = TextLight,
    surfaceVariant = Color(0xFF17212F),
    onSurfaceVariant = TextDim,
    outline = Color(0xFF2A3646),
    outlineVariant = Color(0x1AFFFFFF),
    error = CoralBright,
    onError = Color(0xFF2B0006),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4C6B00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F79A),
    onPrimaryContainer = Color(0xFF1A2400),
    secondary = Color(0xFF007A8A),
    onSecondary = Color.White,
    tertiary = Color(0xFF9C5C00),
    onTertiary = Color.White,
    background = Color(0xFFF5F7EF),
    onBackground = Color(0xFF10160D),
    surface = Color.White,
    onSurface = Color(0xFF10160D),
    surfaceVariant = Color(0xFFF0F3E8),
    onSurfaceVariant = Color(0xFF5C6759),
    outline = Color(0xFFC3CCB8),
    outlineVariant = Color(0x1410180F),
    error = Color(0xFFC7354A),
    onError = Color.White,
)

@Composable
fun WarehouseTheme(
    theme: String = "dark",
    content: @Composable () -> Unit,
) {
    val dark = when (theme) {
        "light" -> false
        "system" -> isSystemInDarkTheme()
        else -> true
    }
    val colors = if (dark) DarkAppColors else LightAppColors
    val scheme = if (dark) DarkScheme else LightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SkladTypography,
            content = content,
        )
    }
}
