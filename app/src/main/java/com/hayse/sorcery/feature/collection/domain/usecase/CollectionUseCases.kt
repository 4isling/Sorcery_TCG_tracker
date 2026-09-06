package com.hayse.sorcery.feature.collection.domain.usecase

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.ImportReport
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow

class ObserveCollectionUseCase(private val repository: CollectionRepository) {
    operator fun invoke(filter: CollectionFilter): Flow<List<SetEntry<CollectionItem>>> =
        repository.observeCollection(filter)
}

class ObserveOwnedForCardUseCase(private val repository: CollectionRepository) {
    operator fun invoke(cardName: String): Flow<List<OwnedCopy>> =
        repository.observeOwnedForCard(cardName)
}

class ObserveSetCompletionUseCase(private val repository: CollectionRepository) {
    operator fun invoke(): Flow<List<SetCompletion>> = repository.observeSetCompletion()
}

class ObserveSurplusUseCase(private val repository: CollectionRepository) {
    operator fun invoke(): Flow<List<SurplusCard>> = repository.observeSurplus()
}

class ObserveMissingUseCase(private val repository: CollectionRepository) {
    operator fun invoke(setName: String): Flow<List<Card>> = repository.observeMissing(setName)
}

class SetQuantityUseCase(private val repository: CollectionRepository) {
    suspend operator fun invoke(slug: String, finish: String, quantity: Int) =
        repository.setQuantity(slug, finish, quantity)
}

class AdjustQuantityUseCase(private val repository: CollectionRepository) {
    suspend operator fun invoke(slug: String, finish: String, delta: Int) =
        repository.adjustQuantity(slug, finish, delta)
}

class ImportCuriosaUseCase(private val repository: CollectionRepository) {
    suspend operator fun invoke(csv: String, replace: Boolean): ImportReport =
        repository.importCuriosa(csv, replace)
}
