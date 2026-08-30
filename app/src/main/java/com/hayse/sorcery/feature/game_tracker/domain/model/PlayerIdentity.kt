package com.hayse.sorcery.feature.game_tracker.domain.model

/** Identité visuelle d'un joueur pour une partie : avatar choisi (nom + image) et pseudo optionnel. */
data class PlayerIdentity(
    val avatarName: String? = null,
    val avatarImageUri: String? = null,
    val pseudo: String? = null,
)
