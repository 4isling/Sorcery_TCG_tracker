package com.hayse.sorcery.feature.game_tracker.data.repository

import androidx.datastore.core.DataStore
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameStateData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toDomain
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameSessionRepositoryImpl(
    private val dataStore: DataStore<GameStateData?>,
) : GameSessionRepository {

    override val gameState: Flow<GameState?> = dataStore.data.map { it?.toDomain() }

    override suspend fun save(state: GameState) {
        dataStore.updateData { state.toData() }
    }

    override suspend fun clear() {
        dataStore.updateData { null }
    }
}
