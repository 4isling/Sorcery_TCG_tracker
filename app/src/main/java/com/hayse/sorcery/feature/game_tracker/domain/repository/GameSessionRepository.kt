package com.hayse.sorcery.feature.game_tracker.domain.repository

import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import kotlinx.coroutines.flow.Flow

interface GameSessionRepository {
    /** Émet l'état de partie persisté ; null tant qu'aucune partie n'a été démarrée. */
    val gameState: Flow<GameState?>

    suspend fun save(state: GameState)

    suspend fun clear()
}
