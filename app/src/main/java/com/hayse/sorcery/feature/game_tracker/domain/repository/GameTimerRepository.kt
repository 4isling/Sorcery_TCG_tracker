package com.hayse.sorcery.feature.game_tracker.domain.repository

import com.hayse.sorcery.feature.game_tracker.domain.model.GameTimerState
import kotlinx.coroutines.flow.Flow

/**
 * État du chrono persisté avec l'instant du dernier tick ([lastTickEpochMs], horloge murale), pour
 * pouvoir rattraper le temps écoulé pendant que l'app était fermée.
 */
data class GameTimerSnapshot(
    val state: GameTimerState,
    val lastTickEpochMs: Long,
)

interface GameTimerRepository {
    /** Émet le chrono persisté ; null tant qu'aucune partie chronométrée n'est en cours. */
    val snapshot: Flow<GameTimerSnapshot?>

    suspend fun save(snapshot: GameTimerSnapshot?)

    suspend fun clear()
}
