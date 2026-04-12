package com.partitionsoft.bookshelf.ui.theme

import androidx.compose.material.Typography
import androidx.compose.material3.Typography as Typography3
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.SansSerif

private fun textStyle(
    weight: FontWeight,
    sizeSp: Int,
    lineHeightSp: Int,
    letterSpacingSp: Double = 0.0
): TextStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = sizeSp.sp,
    lineHeight = lineHeightSp.sp,
    letterSpacing = letterSpacingSp.sp
)

val InkwellTypography = Typography3(
    displayLarge = textStyle(FontWeight.Normal, 57, 64, -0.25),
    displayMedium = textStyle(FontWeight.Normal, 45, 52),
    displaySmall = textStyle(FontWeight.SemiBold, 36, 44),
    headlineLarge = textStyle(FontWeight.SemiBold, 32, 40),
    headlineMedium = textStyle(FontWeight.SemiBold, 28, 36),
    headlineSmall = textStyle(FontWeight.SemiBold, 24, 32),
    titleLarge = textStyle(FontWeight.SemiBold, 22, 28),
    titleMedium = textStyle(FontWeight.Medium, 16, 24, 0.15),
    titleSmall = textStyle(FontWeight.Medium, 14, 20, 0.1),
    bodyLarge = textStyle(FontWeight.Normal, 16, 24, 0.5),
    bodyMedium = textStyle(FontWeight.Normal, 14, 20, 0.25),
    bodySmall = textStyle(FontWeight.Normal, 12, 16, 0.4),
    labelLarge = textStyle(FontWeight.SemiBold, 14, 20, 0.1),
    labelMedium = textStyle(FontWeight.Medium, 12, 16, 0.5),
    labelSmall = textStyle(FontWeight.Medium, 11, 16, 0.5)
)

// Material 2 bridge for legacy screens not migrated to M3 yet.
val Typography = Typography(
    h6 = InkwellTypography.titleLarge,
    subtitle1 = InkwellTypography.titleMedium,
    body1 = InkwellTypography.bodyLarge,
    body2 = InkwellTypography.bodyMedium,
    caption = InkwellTypography.bodySmall,
    button = InkwellTypography.labelLarge,
    overline = InkwellTypography.labelSmall
)