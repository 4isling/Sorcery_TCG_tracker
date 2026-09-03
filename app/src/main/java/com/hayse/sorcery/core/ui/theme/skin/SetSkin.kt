package com.hayse.sorcery.core.ui.theme.skin

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * Couche décorative appliquée par set au chrome de l'app (fond, top bar, cartes, titres).
 * Les couleurs restent gérées séparément par [com.hayse.sorcery.core.ui.theme.paletteFor].
 */
data class SetSkin(
    val id: String,
    val displayFont: FontFamily,
    val shapes: Shapes,
    val cardBorder: BorderStroke?,
    val motif: SkinMotif,
)

sealed interface SkinMotif {
    data object None : SkinMotif
    data class Noise(val alpha: Float) : SkinMotif
    data class DropCap(val color: Color) : SkinMotif
    data class Scales(val stroke: Color, val alpha: Float) : SkinMotif
    data class GoldFrame(val color: Color) : SkinMotif
}
