package com.roadlens.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandCyan,
    onPrimary = BrandNavyDark,
    primaryContainer = BrandNavySurface,
    onPrimaryContainer = Color.White,
    secondary = BrandSkyBlue,
    onSecondary = Color.White,
    tertiary = BrandAmber,
    onTertiary = Color.White,
    surface = BrandNavyDark,
    onSurface = Color.White,
    background = BrandNavy,
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = BrandNavySurface,
    onPrimaryContainer = Color.White,
    secondary = BrandTeal,
    onSecondary = Color.White,
    tertiary = BrandAmber,
    onTertiary = Color.White,
    surface = Color.White,
    onSurface = SlateDark,
    background = BackgroundNeutral,
    onBackground = SlateDark
)

@Composable
fun RoadLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
