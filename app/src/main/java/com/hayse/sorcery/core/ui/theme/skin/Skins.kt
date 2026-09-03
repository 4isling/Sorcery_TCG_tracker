package com.hayse.sorcery.core.ui.theme.skin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.ui.theme.SorcerySet
import com.hayse.sorcery.core.ui.theme.SorceryShapes

private val GothicShapes = Shapes(
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(4.dp),
)

private val DragonlordShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
)

object Skins {
    val Default = SetSkin(
        id = "default",
        displayFont = FontFamily.Default,
        shapes = SorceryShapes,
        cardBorder = null,
        motif = SkinMotif.None,
    )

    val Beta = SetSkin(
        id = "beta",
        displayFont = CinzelFamily,
        shapes = SorceryShapes,
        cardBorder = BorderStroke(0.5.dp, Color(0xFF2A3040)),
        motif = SkinMotif.GoldFrame(Color(0xFFC9A24A)),
    )

    val Arthurian = SetSkin(
        id = "arthurian",
        displayFont = UncialAntiquaFamily,
        shapes = SorceryShapes,
        cardBorder = BorderStroke(0.5.dp, Color(0xFFC79A3E)),
        motif = SkinMotif.DropCap(Color(0xFFD2622C)),
    )

    val Gothic = SetSkin(
        id = "gothic",
        displayFont = PirataOneFamily,
        shapes = GothicShapes,
        cardBorder = BorderStroke(0.5.dp, Color(0xFF3A2A4A)),
        motif = SkinMotif.Noise(0.12f),
    )

    val Dragonlord = SetSkin(
        id = "dragonlord",
        displayFont = MetamorphousFamily,
        shapes = DragonlordShapes,
        cardBorder = BorderStroke(0.5.dp, Color(0xFFE0A63A)),
        motif = SkinMotif.Scales(Color(0xFF2F4A38), 0.35f),
    )

    /** Skins distincts pour previews/tests (Alpha≈Beta, Promotional≈Default sont omis). */
    val all: List<SetSkin> = listOf(Default, Beta, Arthurian, Gothic, Dragonlord)
}

fun skinFor(set: SorcerySet?): SetSkin = when (set) {
    null -> Skins.Default
    SorcerySet.Alpha -> Skins.Beta
    SorcerySet.Beta -> Skins.Beta
    SorcerySet.ArthurianLegends -> Skins.Arthurian
    SorcerySet.Gothic -> Skins.Gothic
    SorcerySet.Dragonlord -> Skins.Dragonlord
    SorcerySet.Promotional -> Skins.Default
}
