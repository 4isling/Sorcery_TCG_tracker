package com.hayse.sorcery.feature.cards.data.repository

import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.feature.cards.data.local.CardCatalogSeeder
import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CardRepositoryImpl(
    private val dao: CardDao,
    private val collectionDao: CollectionDao,
    private val seeder: CardCatalogSeeder,
    private val imageResolver: CardImageResolver,
) : CardRepository {

    override suspend fun ensureSeeded() = seeder.ensureSeeded()

    override fun observeCards(filter: CardFilter): Flow<List<SetEntry<Card>>> =
        combine(
            dao.observeCards(
                query = filter.query?.takeIf { it.isNotBlank() },
                // Neutre/Multi ne s'expriment pas en SQL : filtre appliqué en Kotlin ci-dessous.
                element = null,
                type = filter.type,
                rarity = filter.rarity?.name,
                setName = filter.setName,
            ),
            collectionDao.observeEntries(),
        ) { rows, entries ->
            val quantityBySlug = entries.associate { it.printingSlug to it.quantity }
            rows.asSequence()
                .filter { filter.ownership == Ownership.All || keepByOwnership(it, quantityBySlug, filter.ownership) }
                .flatMap { cwp -> toSetEntries(cwp, filter).asSequence() }
                .filter { filter.element.matches(it.card.elements) }
                .toList()
        }

    private fun toSetEntries(cwp: CardWithPrintings, filter: CardFilter): List<SetEntry<Card>> {
        val card = cwp.toCard(imageResolver::imageUriForSlugs)
        val sets = if (filter.setName != null) listOf(filter.setName)
        else cwp.printings.map { it.setName }.distinct()
        return sets.map { SetEntry(it, card, card) }
    }

    private fun keepByOwnership(
        cwp: CardWithPrintings,
        quantityBySlug: Map<String, Int>,
        ownership: Ownership,
    ): Boolean {
        val totalOwned = cwp.printings.sumOf { quantityBySlug[it.slug] ?: 0 }
        return when (ownership) {
            Ownership.All -> true
            Ownership.Owned -> totalOwned > 0
            Ownership.Missing -> totalOwned == 0
            Ownership.Surplus -> {
                val max = cwp.toCard(imageResolver::imageUriForSlugs).rarity?.maxCopies ?: Int.MAX_VALUE
                totalOwned > max
            }
        }
    }

    override suspend fun getCard(name: String): CardDetail? =
        dao.getCardWithPrintings(name)?.toDetail(imageResolver::imageUriForSlugs)

    override suspend fun availableTypes(): List<String> = dao.distinctTypes()

    override suspend fun availableSets(): List<String> = dao.distinctSets()
}
