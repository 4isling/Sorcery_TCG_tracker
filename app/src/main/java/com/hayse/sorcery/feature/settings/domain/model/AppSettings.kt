package com.hayse.sorcery.feature.settings.domain.model

import com.hayse.sorcery.core.ui.theme.SorcerySet

/** Mode d'affichage clair/sombre choisi par l'utilisateur. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Portée du skin décoratif par set. FIXED applique le skin de [AppSettings.themeSet] ;
 * OFF neutralise la décoration (les couleurs du set restent inchangées).
 */
enum class SkinMode { OFF, FIXED }

/** Langue de l'app. `localeTag` null = suit la langue du système. */
enum class AppLanguage(val localeTag: String?) {
    SYSTEM(null),
    FRENCH("fr"),
    ENGLISH("en"),
    SPANISH("es"),
    CATALAN("ca"),
}

/**
 * Préférences globales de l'app. `themeSet` null = palette par défaut ; sinon la palette du set
 * teinte l'ensemble des écrans (voir [com.hayse.sorcery.core.ui.theme.SorceryTheme]).
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeSet: SorcerySet? = null,
    /** Portée du skin décoratif (voir [SkinMode]). */
    val skinMode: SkinMode = SkinMode.FIXED,
    /** Active le module social/échanges (désactivable pour garder le cœur autonome). */
    val socialEnabled: Boolean = true,
    /** Pseudo affiché dans les rooms/chat. Vide = nom de l'appareil par défaut. */
    val pseudo: String = "",
    /** Identité stable de l'appareil sur le maillage social (UUID). Vide tant que non généré. */
    val deviceId: String = "",
)
