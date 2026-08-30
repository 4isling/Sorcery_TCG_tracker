package com.hayse.sorcery.feature.settings.domain.model

import com.hayse.sorcery.core.ui.theme.SorcerySet

/** Mode d'affichage clair/sombre choisi par l'utilisateur. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Langue de l'app. `localeTag` null = suit la langue du système. */
enum class AppLanguage(val localeTag: String?) {
    SYSTEM(null),
    FRENCH("fr"),
    ENGLISH("en"),
}

/**
 * Préférences globales de l'app. `themeSet` null = palette par défaut ; sinon la palette du set
 * teinte l'ensemble des écrans (voir [com.hayse.sorcery.core.ui.theme.SorceryTheme]).
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeSet: SorcerySet? = null,
)
