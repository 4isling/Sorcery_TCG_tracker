package com.hayse.sorcery.feature.settings.data.local.model

import kotlinx.serialization.Serializable

/**
 * Représentation persistée des préférences (DataStore). Les enums sont stockés par nom pour rester
 * lisibles et rétro-compatibles ; une valeur inconnue retombe sur le défaut au mapping.
 */
@Serializable
data class SettingsData(
    val themeMode: String = "SYSTEM",
    val language: String = "SYSTEM",
    val themeSet: String? = null,
    val skinMode: String = "FIXED",
    val socialEnabled: Boolean = true,
    val pseudo: String = "",
    val deviceId: String = "",
)
