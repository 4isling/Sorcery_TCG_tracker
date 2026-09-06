package com.hayse.sorcery.feature.collection.domain.repository

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.ImportReport
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {

    fun observeCollection(filter: CollectionFilter): Flow<List<SetEntry<CollectionItem>>>

    fun observeOwnedForCard(cardName: String): Flow<List<OwnedCopy>>

    fun observeSetCompletion(): Flow<List<SetCompletion>>

    fun observeSurplus(): Flow<List<SurplusCard>>

    fun observeMissing(setName: String): Flow<List<Card>>

    suspend fun setQuantity(slug: String, finish: String, quantity: Int)

    suspend fun adjustQuantity(slug: String, finish: String, delta: Int)

    /** Importe un CSV Curiosa. `replace` = vider la collection avant. */
    suspend fun importCuriosa(csv: String, replace: Boolean): ImportReport
}
