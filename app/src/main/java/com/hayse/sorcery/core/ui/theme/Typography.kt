package com.hayse.sorcery.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

private val base = Typography()

/** Typographie de base où seuls display/headline/title prennent [displayFont] ; corps en système. */
fun sorceryTypography(displayFont: FontFamily): Typography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = displayFont),
    displayMedium = base.displayMedium.copy(fontFamily = displayFont),
    displaySmall = base.displaySmall.copy(fontFamily = displayFont),
    headlineLarge = base.headlineLarge.copy(fontFamily = displayFont),
    headlineMedium = base.headlineMedium.copy(fontFamily = displayFont),
    headlineSmall = base.headlineSmall.copy(fontFamily = displayFont),
    titleLarge = base.titleLarge.copy(fontFamily = displayFont),
)
