package com.hayse.sorcery.feature.game_tracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface PlayerPrefsRepository {
    /** Pseudo par défaut du joueur 1 (propriétaire de l'app), null si non défini. */
    val ownerPseudo: Flow<String?>

    suspend fun setOwnerPseudo(pseudo: String?)
}
