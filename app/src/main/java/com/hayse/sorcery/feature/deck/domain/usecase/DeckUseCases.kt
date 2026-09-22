package com.hayse.sorcery.feature.deck.domain.usecase

import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckDetail
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow

class ObserveDecksUseCase(private val repository: DeckRepository) {
    operator fun invoke(): Flow<List<DeckSummary>> = repository.observeDecks()
}

class ObserveDeckUseCase(private val repository: DeckRepository) {
    operator fun invoke(deckId: Long): Flow<DeckDetail?> = repository.observeDeck(deckId)
}

class ObserveDeckCatalogUseCase(private val repository: DeckRepository) {
    operator fun invoke(deckId: Long, filter: DeckCatalogFilter): Flow<List<DeckEntry>> =
        repository.observeCatalog(deckId, filter)
}

class CreateDeckUseCase(private val repository: DeckRepository) {
    suspend operator fun invoke(name: String, format: DeckFormat): Long =
        repository.createDeck(name, format)
}

class RenameDeckUseCase(private val repository: DeckRepository) {
    suspend operator fun invoke(deckId: Long, name: String) = repository.renameDeck(deckId, name)
}

class DeleteDeckUseCase(private val repository: DeckRepository) {
    suspend operator fun invoke(deckId: Long) = repository.deleteDeck(deckId)
}

class SetDeckCardQuantityUseCase(private val repository: DeckRepository) {
    suspend operator fun invoke(deckId: Long, cardName: String, quantity: Int, inCollection: Boolean = false) =
        repository.setCardQuantity(deckId, cardName, quantity, inCollection)
}

class AdjustDeckCardQuantityUseCase(private val repository: DeckRepository) {
    suspend operator fun invoke(deckId: Long, cardName: String, delta: Int, inCollection: Boolean = false) =
        repository.adjustCardQuantity(deckId, cardName, delta, inCollection)
}
