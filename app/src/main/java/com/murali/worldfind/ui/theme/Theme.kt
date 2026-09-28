package com.murali.worldfind.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// WORLD FIND DARK THEME
// ============================================================

private val WorldDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = WorldTextSecondary,
    onSecondary = Color.Black,
    tertiary = Color.White,
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = WorldSurface,
    onSurface = Color.White,
    surfaceVariant = WorldSurfaceVariant,
    onSurfaceVariant = WorldTextSecondary,
    outline = WorldBorder,
    outlineVariant = WorldDivider,
    error = WorldError,
    onError = Color.White
)

// ============================================================
// WORLD FIND LIGHT THEME
// ============================================================

private val WorldLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = WorldLightTextSecondary,
    onSecondary = Color.White,
    tertiary = Color.Black,
    onTertiary = Color.White,
    background = WorldLightBackground,
    onBackground = Color.Black,
    surface = WorldLightSurface,
    onSurface = Color.Black,
    surfaceVariant = WorldLightCard,
    onSurfaceVariant = WorldLightTextSecondary,
    outline = WorldLightBorder,
    outlineVariant = WorldLightBorder,
    error = WorldError,
    onError = Color.White
)

// ============================================================
// WORLD FIND THEME
// ============================================================

@Composable
fun WorldFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WorldDarkColorScheme else WorldLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WorldFindTypography,
        content = content
    )
}
