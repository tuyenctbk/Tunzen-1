package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TuneZenColors(
    val background: Color,
    val surfaceCard: Color,
    val surfaceCardVariant: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val emeraldInTune: Color,
    val amberFlat: Color,
    val rubySharp: Color
)

val OledDarkColors = TuneZenColors(
    background = Color(0xFF101114),
    surfaceCard = Color(0xFF16181D),
    surfaceCardVariant = Color(0xFF22252C),
    surfaceBorder = Color(0xFF2E333F),
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFF9CA3AF),
    textMuted = Color(0xFF6B7280),
    accent = Color(0xFF10B981),
    emeraldInTune = Color(0xFF10B981),
    amberFlat = Color(0xFFF59E0B),
    rubySharp = Color(0xFFEF4444)
)

val PaperLightColors = TuneZenColors(
    background = Color(0xFFF3F4F6),
    surfaceCard = Color(0xFFFFFFFF),
    surfaceCardVariant = Color(0xFFE5E7EB),
    surfaceBorder = Color(0xFFD1D5DB),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF374151),
    textMuted = Color(0xFF6B7280),
    accent = Color(0xFF047857),
    emeraldInTune = Color(0xFF059669),
    amberFlat = Color(0xFFD97706),
    rubySharp = Color(0xFFDC2626)
)

val LocalTuneZenColors = staticCompositionLocalOf { OledDarkColors }

val OledBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.background

val SurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.surfaceCard

val SurfaceCardVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.surfaceCardVariant

val SurfaceBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.surfaceBorder

val EmeraldInTune: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.emeraldInTune

val AmberFlat: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.amberFlat

val RubySharp: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.rubySharp

val CyanAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.accent

val CyanAccentDark = Color(0xFF059669)

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneZenColors.current.textMuted
