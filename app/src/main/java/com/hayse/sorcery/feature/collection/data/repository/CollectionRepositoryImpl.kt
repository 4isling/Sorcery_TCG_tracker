package com.hayse.sorcery.feature.collection.data.repository

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.repository.CardImageResolver
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.collection.data.csv.CuriosaCsvParser
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.ImportReport
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CollectionRepositoryImpl(
    private val collectionDao: CollectionDao,
    private val cardDao: CardDao,
    private val imageResolver: CardImageResolver,
) : CollectionRepository {

    private fun observeAllCards() = cardDao.observeCards(null, null, null, null, null)

    override fun observeCollection(filter: CollectionFilter): Flow<List<SetEntry<CollectionItem>>> =
        combine(
            cardDao.observeCards(
                query = filter.query?.takeIf { it.isNotBlank() },
                // Neutre/Multi ne s'expriment pas en SQL : filtre appliqué en Kotlin ci-dessous.
                element = null,
                type = filter.type,
                rarity = filter.rarity?.name,
                setName = filter.setName,
            ),
            collectionDao.observeEntries(),
        ) { cards, entries ->
            CollectionComputations.collectionEntries(
                cards = cards,
                entries = entries,
                ownership = filter.ownership,
                element = filter.element,
                setName = filter.setName,
                imageUriForSlugs = imageResolver::imageUriForSlugs,
            )
        }

    override fun observeOwnedForCard(cardName: String): Flow<List<OwnedCopy>> =
        collectionDao.observeForCard(cardName).map { entries ->
            entries.map { OwnedCopy(it.printingSlug, it.finish, it.quantity) }
        }

    override fun observeSetCompletion(): Flow<List<SetCompletion>> =
        combine(observeAllCards(), collectionDao.observeEntries()) { cards, entries ->
            CollectionComputations.setCompletion(cards, entries)
        }

    override fun observeSurplus(): Flow<List<SurplusCard>> =
        combine(observeAllCards(), collectionDao.observeEntries()) { cards, entries ->
            CollectionComputations.surplus(cards, entries, imageResolver::imageUriForSlugs)
        }

    override fun observeMissing(setName: String): Flow<List<Card>> =
        combine(observeAllCards(), collectionDao.observeEntries()) { cards, entries ->
            CollectionComputations.missing(cards, entries, setName, imageResolver::imageUriForSlugs)
        }

    override suspend fun setQuantity(slug: String, finish: String, quantity: Int) {
        if (quantity <= 0) collectionDao.delete(slug, finish)
        else collectionDao.upsert(CollectionEntryEntity(slug, finish, quantity))
    }

    override suspend fun adjustQuantity(slug: String, finish: String, delta: Int) {
        val current = collectionDao.getQuantity(slug, finish) ?: 0
        setQuantity(slug, finish, current + delta)
    }

    override suspend fun importCuriosa(csv: String, replace: Boolean): ImportReport =
        withContext(Dispatchers.IO) {
            val parsed = CuriosaCsvParser.parse(csv)
            val keys = cardDao.allPrintingKeys()
            val plan = CollectionComputations.matchImport(parsed.rows, keys)
            if (replace) collectionDao.clearAll()
            for (entry in plan.upserts) collectionDao.upsert(entry)
            ImportReport(
                matched = plan.matched,
                unmatched = plan.unmatched,
                ambiguous = plan.ambiguous,
                invalid = parsed.invalid,
            )
        }
}
