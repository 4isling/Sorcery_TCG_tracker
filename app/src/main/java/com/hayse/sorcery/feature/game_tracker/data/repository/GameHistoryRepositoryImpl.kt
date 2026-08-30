package com.hayse.sorcery.feature.game_tracker.data.repository

import androidx.datastore.core.DataStore
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameHistoryData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toDomain
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameHistoryRepositoryImpl(
    private val dataStore: DataStore<GameHistoryData>,
) : GameHistoryRepository {

    override val games: Flow<List<GameRecord>> = dataStore.data.map { data ->
        data.records.map { it.toDomain() }.sortedByDescending { it.playedAt }
    }

    override suspend fun add(record: GameRecord) {
        dataStore.updateData { current ->
            current.copy(records = current.records + record.toData())
        }
    }

    override suspend fun clear() {
        dataStore.updateData { GameHistoryData() }
    }
}
