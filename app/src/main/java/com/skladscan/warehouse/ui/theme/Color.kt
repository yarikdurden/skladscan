package com.skladscan.warehouse.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val Lime = Color(0xFFC6FF4A)
val LimeDeep = Color(0xFF8FD400)
val LimeMint = Color(0xFF7BF0B4)
val Ink = Color(0xFF070A0F)
val InkElevated = Color(0xFF0C121B)
val CyanBright = Color(0xFF63E6FF)
val AmberBright = Color(0xFFFFC24B)
val CoralBright = Color(0xFFFF6B7A)
val VioletBright = Color(0xFFB69CFF)
val TextLight = Color(0xFFF2F6E9)
val TextDim = Color(0xFF98A4B2)

@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundElevated: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val stroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val accentFill: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val cyan: Color,
    val amber: Color,
    val coral: Color,
    val violet: Color,
    val chartGrid: Color,
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Ink,
    backgroundElevated = InkElevated,
    surface = Color(0xFF101722),
    surfaceHigh = Color(0xFF17212F),
    stroke = Color(0x1AFFFFFF),
    textPrimary = TextLight,
    textSecondary = TextDim,
    textMuted = Color(0xFF697585),
    accent = Lime,
    accentFill = Lime,
    accentInk = Color(0xFF101505),
    accentSoft = Color(0x1FC6FF4A),
    cyan = CyanBright,
    amber = AmberBright,
    coral = CoralBright,
    violet = VioletBright,
    chartGrid = Color(0x14FFFFFF),
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF5F7EF),
    backgroundElevated = Color(0xFFEDF1E3),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFF0F3E8),
    stroke = Color(0x1410180F),
    textPrimary = Color(0xFF10160D),
    textSecondary = Color(0xFF5C6759),
    textMuted = Color(0xFF899485),
    accent = Color(0xFF4C6B00),
    accentFill = Lime,
    accentInk = Color(0xFF101505),
    accentSoft = Color(0x1F6B8F00),
    cyan = Color(0xFF007A8A),
    amber = Color(0xFFB26A00),
    coral = Color(0xFFC7354A),
    violet = Color(0xFF7057C8),
    chartGrid = Color(0x1410180F),
)
