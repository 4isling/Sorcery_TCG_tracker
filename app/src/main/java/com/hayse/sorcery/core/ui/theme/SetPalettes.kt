package com.hayse.sorcery.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Paires clair/sombre par set. Fallback sur la palette de base de l'app. */
data class SetPalette(val light: ColorScheme, val dark: ColorScheme)

// Palette par défaut : identité de base de l'app (brun/teal parchemin).
val DefaultPalette = SetPalette(light = LightColorScheme, dark = DarkColorScheme)

// Alpha reprend la base historique de l'app.
private val AlphaPalette = DefaultPalette

// Beta : bleu sombre, encres nocturnes.
private val BetaPalette = SetPalette(
    light = lightColorScheme(
        primary = Color(0xFF1E3A5F),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF3E6EA5),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFEAF0F7),
        surface = Color(0xFFF4F8FD),
        error = Color(0xFFB3261E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFF7FB2E8),
        onPrimary = Color(0xFF0A1B2E),
        secondary = Color(0xFF9EC3E8),
        onSecondary = Color(0xFF0A1B2E),
        background = Color(0xFF0A1220),
        surface = Color(0xFF111C2E),
        error = Color(0xFFF2B8B5),
    ),
)

// Arthurian Legends : doré médiéval sur parchemin.
private val ArthurianPalette = SetPalette(
    light = lightColorScheme(
        primary = Color(0xFF9A7B2E),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFB8912F),
        onSecondary = Color(0xFF2B2100),
        background = Color(0xFFF7EFD9),
        surface = Color(0xFFFCF6E6),
        error = Color(0xFFB3261E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFE9C86A),
        onPrimary = Color(0xFF3A2E00),
        secondary = Color(0xFFD4B15A),
        onSecondary = Color(0xFF3A2E00),
        background = Color(0xFF191509),
        surface = Color(0xFF241E0E),
        error = Color(0xFFF2B8B5),
    ),
)

// Gothic : cramoisi, noir, gris et blanc.
private val GothicPalette = SetPalette(
    light = lightColorScheme(
        primary = Color(0xFF8E1B1B),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF4A4A4A),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFF2F0F0),
        surface = Color(0xFFFAFAFA),
        error = Color(0xFFB3261E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFE05353),
        onPrimary = Color(0xFF2A0606),
        secondary = Color(0xFFB8B8B8),
        onSecondary = Color(0xFF1A1A1A),
        background = Color(0xFF0D0B0B),
        surface = Color(0xFF171313),
        error = Color(0xFFF2B8B5),
    ),
)

// Dragonlord : braise et feu (dérivé).
private val DragonlordPalette = SetPalette(
    light = lightColorScheme(
        primary = Color(0xFFB5451B),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFD2691E),
        onSecondary = Color(0xFF2B1000),
        background = Color(0xFFF7EDE6),
        surface = Color(0xFFFCF4EE),
        error = Color(0xFFB3261E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFF08A4B),
        onPrimary = Color(0xFF351200),
        secondary = Color(0xFFE0A66A),
        onSecondary = Color(0xFF351200),
        background = Color(0xFF1A1009),
        surface = Color(0xFF261811),
        error = Color(0xFFF2B8B5),
    ),
)

// Promotional : violet feutré et argent (dérivé neutre).
private val PromotionalPalette = SetPalette(
    light = lightColorScheme(
        primary = Color(0xFF5E4B8B),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF7A6CA8),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFF1EFF6),
        surface = Color(0xFFF8F6FC),
        error = Color(0xFFB3261E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFBBA6E8),
        onPrimary = Color(0xFF261A45),
        secondary = Color(0xFFCFC3E8),
        onSecondary = Color(0xFF261A45),
        background = Color(0xFF12101A),
        surface = Color(0xFF1C1826),
        error = Color(0xFFF2B8B5),
    ),
)

fun paletteFor(set: SorcerySet?): SetPalette = when (set) {
    SorcerySet.Alpha -> AlphaPalette
    SorcerySet.Beta -> BetaPalette
    SorcerySet.ArthurianLegends -> ArthurianPalette
    SorcerySet.Gothic -> GothicPalette
    SorcerySet.Dragonlord -> DragonlordPalette
    SorcerySet.Promotional -> PromotionalPalette
    null -> DefaultPalette
}
