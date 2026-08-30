package com.hayse.sorcery.feature.game_tracker.data.repository

import androidx.datastore.core.DataStore
import com.hayse.sorcery.feature.game_tracker.data.local.model.PlayerPrefsData
import com.hayse.sorcery.feature.game_tracker.domain.repository.PlayerPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerPrefsRepositoryImpl(
    private val dataStore: DataStore<PlayerPrefsData>,
) : PlayerPrefsRepository {

    override val ownerPseudo: Flow<String?> = dataStore.data.map { it.ownerPseudo }

    override suspend fun setOwnerPseudo(pseudo: String?) {
        dataStore.updateData { it.copy(ownerPseudo = pseudo?.takeIf(String::isNotBlank)) }
    }
}
