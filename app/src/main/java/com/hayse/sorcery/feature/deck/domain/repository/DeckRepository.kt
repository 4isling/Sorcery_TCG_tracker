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

    /**
     * Catalogue complet (filtré par [filter]) avec, pour chaque carte, sa quantité dans le deck et la
     * possession. Si la section filtrée est la Collection, la quantité est celle de la Collection ;
     * sinon celle du deck principal.
     */
    fun observeCatalog(deckId: Long, filter: DeckCatalogFilter): Flow<List<DeckEntry>>

    suspend fun availableTypes(): List<String>

    suspend fun availableSets(): List<String>

    suspend fun createDeck(name: String, format: DeckFormat): Long

    suspend fun renameDeck(deckId: Long, name: String)

    suspend fun deleteDeck(deckId: Long)

    /** Fixe le nombre de copies dans le deck principal ou, si [inCollection], dans la Collection. */
    suspend fun setCardQuantity(deckId: Long, cardName: String, quantity: Int, inCollection: Boolean = false)

    suspend fun adjustCardQuantity(deckId: Long, cardName: String, delta: Int, inCollection: Boolean = false)
}
