package com.hayse.sorcery.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.hayse.sorcery.core.shared.model.Element

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6D4C41),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF00695C),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F0E6),
    surface = Color(0xFFFFFBF3),
    error = Color(0xFFB3261E),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD7B49E),
    onPrimary = Color(0xFF3E2723),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003731),
    background = Color(0xFF161311),
    surface = Color(0xFF211D1A),
    error = Color(0xFFF2B8B5),
)

val ElementAir = Color(0xFF90A4AE)
val ElementEarth = Color(0xFF8D6E63)
val ElementFire = Color(0xFFE53935)
val ElementWater = Color(0xFF1E88E5)

fun Element.color(): Color = when (this) {
    Element.Air -> ElementAir
    Element.Earth -> ElementEarth
    Element.Fire -> ElementFire
    Element.Water -> ElementWater
}
