package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun TuneZenTheme(
    isPaperLightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val tuneZenColors = if (isPaperLightMode) PaperLightColors else OledDarkColors
    val colorScheme = if (isPaperLightMode) {
        lightColorScheme(
            primary = tuneZenColors.accent,
            onPrimary = Color.White,
            primaryContainer = tuneZenColors.surfaceCardVariant,
            onPrimaryContainer = tuneZenColors.textPrimary,
            secondary = tuneZenColors.emeraldInTune,
            onSecondary = Color.White,
            tertiary = tuneZenColors.amberFlat,
            background = tuneZenColors.background,
            onBackground = tuneZenColors.textPrimary,
            surface = tuneZenColors.surfaceCard,
            onSurface = tuneZenColors.textPrimary,
            surfaceVariant = tuneZenColors.surfaceCardVariant,
            onSurfaceVariant = tuneZenColors.textSecondary,
            outline = tuneZenColors.surfaceBorder
        )
    } else {
        darkColorScheme(
            primary = tuneZenColors.accent,
            onPrimary = tuneZenColors.background,
            primaryContainer = tuneZenColors.surfaceCardVariant,
            onPrimaryContainer = tuneZenColors.textPrimary,
            secondary = tuneZenColors.emeraldInTune,
            onSecondary = tuneZenColors.background,
            tertiary = tuneZenColors.amberFlat,
            background = tuneZenColors.background,
            onBackground = tuneZenColors.textPrimary,
            surface = tuneZenColors.surfaceCard,
            onSurface = tuneZenColors.textPrimary,
            surfaceVariant = tuneZenColors.surfaceCardVariant,
            onSurfaceVariant = tuneZenColors.textSecondary,
            outline = tuneZenColors.surfaceBorder
        )
    }

    CompositionLocalProvider(LocalTuneZenColors provides tuneZenColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TuneZenTypography,
            content = content
        )
    }
}
