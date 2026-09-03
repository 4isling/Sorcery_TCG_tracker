package com.hayse.sorcery.core.ui.theme.skin

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.hayse.sorcery.R

// Polices d'affichage téléchargeables (Google Fonts, pas de .ttf embarqué).
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun displayFamily(name: String): FontFamily {
    val font = GoogleFont(name)
    return FontFamily(
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.Normal),
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    )
}

val CinzelFamily: FontFamily = displayFamily("Cinzel")
val UncialAntiquaFamily: FontFamily = displayFamily("Uncial Antiqua")
val PirataOneFamily: FontFamily = displayFamily("Pirata One")
val MetamorphousFamily: FontFamily = displayFamily("Metamorphous")
