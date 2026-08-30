package com.hayse.sorcery.feature.game_tracker.data.local.model

import kotlinx.serialization.Serializable

/** Préférences persistantes du joueur propriétaire de l'app (joueur 1). */
@Serializable
data class PlayerPrefsData(
    val ownerPseudo: String? = null,
)
