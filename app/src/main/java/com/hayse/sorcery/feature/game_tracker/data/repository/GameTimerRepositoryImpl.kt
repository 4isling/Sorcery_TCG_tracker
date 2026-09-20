package com.hayse.sorcery.feature.game_tracker.data.repository

import androidx.datastore.core.DataStore
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameTimerData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toDomain
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameTimerRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameTimerSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameTimerRepositoryImpl(
    private val dataStore: DataStore<GameTimerData?>,
) : GameTimerRepository {

    override val snapshot: Flow<GameTimerSnapshot?> = dataStore.data.map { it?.toDomain() }

    override suspend fun save(snapshot: GameTimerSnapshot?) {
        dataStore.updateData { snapshot?.toData() }
    }

    override suspend fun clear() {
        dataStore.updateData { null }
    }
}
