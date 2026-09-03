package com.hayse.sorcery.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Test
import kotlin.math.pow

/** Vérifie que le texte reste lisible (WCAG AA ≥ 4.5:1) sur chaque palette sombre. */
class PaletteContrastTest {

    private fun channel(c: Float): Double =
        if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).toDouble().pow(2.4)

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun contrast(fg: Color, bg: Color): Double {
        val l1 = luminance(fg)
        val l2 = luminance(bg)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertReadable(scheme: ColorScheme, setName: String) {
        val onBg = contrast(scheme.onBackground, scheme.background)
        val onSurf = contrast(scheme.onSurface, scheme.surface)
        assert(onBg >= 4.5) { "$setName onBackground/background = $onBg < 4.5" }
        assert(onSurf >= 4.5) { "$setName onSurface/surface = $onSurf < 4.5" }
    }

    @Test
    fun `chaque palette sombre respecte le contraste AA`() {
        SorcerySet.entries.forEach { set ->
            assertReadable(paletteFor(set).dark, set.name)
        }
        assertReadable(DefaultPalette.dark, "Default")
    }
}
