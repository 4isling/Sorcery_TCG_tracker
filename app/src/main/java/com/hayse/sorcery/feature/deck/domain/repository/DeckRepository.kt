package com.hayse.sorcery.feature.deck.domain.repository

import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckDetail
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun observeDecks(): Flow<List<DeckSummary>>

    fun observeDeck(deckId: Long): Flow<DeckDetail?>

    /** Catalogue complet (filtré par [filter]) avec, pour chaque carte, sa quantité dans le deck et la possession. */
    fun observeCatalog(deckId: Long, filter: DeckCatalogFilter): Flow<List<DeckEntry>>

    suspend fun availableTypes(): List<String>

    suspend fun availableSets(): List<String>

    suspend fun createDeck(name: String, format: DeckFormat): Long

    suspend fun renameDeck(deckId: Long, name: String)

    suspend fun deleteDeck(deckId: Long)

    suspend fun setCardQuantity(deckId: Long, cardName: String, quantity: Int)

    suspend fun adjustCardQuantity(deckId: Long, cardName: String, delta: Int)
}
