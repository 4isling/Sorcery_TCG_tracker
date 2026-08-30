package com.hayse.sorcery.feature.game_tracker.domain.repository

import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import kotlinx.coroutines.flow.Flow

interface GameHistoryRepository {
    /** Parties terminées, de la plus récente à la plus ancienne. */
    val games: Flow<List<GameRecord>>

    suspend fun add(record: GameRecord)

    suspend fun clear()
}
