package com.hayse.sorcery.feature.deck.data.repository

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.repository.CardImageResolver
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import com.hayse.sorcery.feature.deck.data.local.dao.DeckDao
import com.hayse.sorcery.feature.deck.data.local.entity.DeckCardEntity
import com.hayse.sorcery.feature.deck.data.local.entity.DeckEntity
import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckDetail
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DeckRepositoryImpl(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val collectionDao: CollectionDao,
    private val imageResolver: CardImageResolver,
) : DeckRepository {

    private fun allCards() = cardDao.observeCards(null, null, null, null, null)

    override fun observeDecks(): Flow<List<DeckSummary>> =
        combine(
            deckDao.observeDecks(),
            deckDao.observeAllDeckCards(),
            allCards(),
            collectionDao.observeEntries(),
        ) { decks, deckCards, cards, entries ->
            DeckComputations.deckSummaries(decks, deckCards, cards, entries, imageResolver::imageUriForSlugs)
        }

    override fun observeDeck(deckId: Long): Flow<DeckDetail?> =
        combine(
            deckDao.observeDeck(deckId),
            deckDao.observeDeckCards(deckId),
            allCards(),
            collectionDao.observeEntries(),
        ) { deck, deckCards, cards, entries ->
            deck.firstOrNull()?.let {
                DeckComputations.deckDetail(it, deckCards, cards, entries, imageResolver::imageUriForSlugs)
            }
        }

    override fun observeCatalog(deckId: Long, filter: DeckCatalogFilter): Flow<List<DeckEntry>> =
        combine(
            cardDao.observeCards(
                filter.query?.takeIf { it.isNotBlank() },
                filter.element?.name,
                filter.type,
                filter.rarity?.name,
                filter.setName,
            ),
            deckDao.observeDeckCards(deckId),
            collectionDao.observeEntries(),
        ) { cards, deckCards, entries ->
            DeckComputations.catalog(
                deckCards,
                cards,
                entries,
                filter.ownership,
                filter.section,
                imageResolver::imageUriForSlugs,
            )
        }

    override suspend fun availableTypes(): List<String> = cardDao.distinctTypes()

    override suspend fun availableSets(): List<String> = cardDao.distinctSets()

    override suspend fun createDeck(name: String, format: DeckFormat): Long {
        val now = System.currentTimeMillis()
        return deckDao.insertDeck(
            DeckEntity(name = name, format = format.id, createdAt = now, updatedAt = now),
        )
    }

    override suspend fun renameDeck(deckId: Long, name: String) =
        deckDao.renameDeck(deckId, name, System.currentTimeMillis())

    override suspend fun deleteDeck(deckId: Long) = deckDao.deleteDeck(deckId)

    override suspend fun setCardQuantity(deckId: Long, cardName: String, quantity: Int) {
        if (quantity <= 0) deckDao.delete(deckId, cardName)
        else deckDao.upsert(DeckCardEntity(deckId, cardName, quantity))
        deckDao.touch(deckId, System.currentTimeMillis())
    }

    override suspend fun adjustCardQuantity(deckId: Long, cardName: String, delta: Int) {
        val current = deckDao.getQuantity(deckId, cardName) ?: 0
        setCardQuantity(deckId, cardName, current + delta)
    }
}
