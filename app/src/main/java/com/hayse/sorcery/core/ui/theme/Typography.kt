package com.hayse.sorcery.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.hayse.sorcery.R

// Fournisseur Google Fonts téléchargeables (pas de .ttf embarqué).
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val cinzel = GoogleFont("Cinzel")

// Cinzel pour les titres (registre médiéval/arthurien) ; corps en police système.
private val CinzelFamily = FontFamily(
    Font(googleFont = cinzel, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = cinzel, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = cinzel, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

private val base = Typography()

val SorceryTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = CinzelFamily),
    displayMedium = base.displayMedium.copy(fontFamily = CinzelFamily),
    displaySmall = base.displaySmall.copy(fontFamily = CinzelFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = CinzelFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = CinzelFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = CinzelFamily),
    titleLarge = base.titleLarge.copy(fontFamily = CinzelFamily),
)
