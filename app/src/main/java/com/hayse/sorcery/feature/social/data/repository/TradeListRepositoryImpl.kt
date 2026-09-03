package com.hayse.sorcery.feature.social.data.repository

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.repository.CardImageResolver
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.cards.data.repository.toDomain
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.Printing
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.social.data.local.dao.SocialDao
import com.hayse.sorcery.feature.social.data.local.entity.TradeableEntryEntity
import com.hayse.sorcery.feature.social.data.local.entity.WantedEntryEntity
import com.hayse.sorcery.feature.social.domain.model.TradeableCopy
import com.hayse.sorcery.feature.social.domain.model.TradeableItem
import com.hayse.sorcery.feature.social.domain.model.WantedCopy
import com.hayse.sorcery.feature.social.domain.model.WantedItem
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TradeListRepositoryImpl(
    private val socialDao: SocialDao,
    private val cardDao: CardDao,
    private val collectionDao: CollectionDao,
    private val imageResolver: CardImageResolver,
) : TradeListRepository {

    private fun observeAllCards() = cardDao.observeCards(null, null, null, null, null)

    override fun observeTradeable(): Flow<List<TradeableItem>> =
        combine(
            collectionDao.observeEntries(),
            socialDao.observeTradeable(),
            observeAllCards(),
        ) { owned, tradeable, cards ->
            val lookup = printingLookup(cards)
            val ownedCopies = owned.map { OwnedCopy(it.printingSlug, it.finish, it.quantity) }
            val storedCopies = tradeable.map { TradeableCopy(it.printingSlug, it.finish, it.quantity) }
            TradeComputations.tradeableRows(ownedCopies, storedCopies).mapNotNull { row ->
                val ref = lookup[row.slug] ?: return@mapNotNull null
                TradeableItem(ref.card, ref.printing, row.ownedQty, row.tradeableQty)
            }
        }

    override fun observeWanted(): Flow<List<WantedItem>> =
        combine(socialDao.observeWanted(), observeAllCards()) { wanted, cards ->
            val lookup = printingLookup(cards)
            val copies = wanted.map { WantedCopy(it.printingSlug, it.finish, it.quantity) }
            TradeComputations.wantedRows(copies).mapNotNull { row ->
                val ref = lookup[row.slug] ?: return@mapNotNull null
                WantedItem(ref.card, ref.printing, row.wantedQty)
            }
        }

    override suspend fun setTradeable(slug: String, finish: String, quantity: Int) {
        val owned = collectionDao.getQuantity(slug, finish) ?: 0
        val clamped = quantity.coerceIn(0, owned)
        if (clamped <= 0) socialDao.deleteTradeable(slug, finish)
        else socialDao.upsertTradeable(TradeableEntryEntity(slug, finish, clamped))
    }

    override suspend fun setWanted(slug: String, finish: String, quantity: Int) {
        if (quantity <= 0) socialDao.deleteWanted(slug, finish)
        else socialDao.upsertWanted(WantedEntryEntity(slug, finish, quantity))
    }

    override suspend fun currentTradeableCopies(): List<TradeableCopy> =
        socialDao.getTradeable().mapNotNull { entry ->
            val owned = collectionDao.getQuantity(entry.printingSlug, entry.finish) ?: 0
            val qty = entry.quantity.coerceAtMost(owned)
            if (qty > 0) TradeableCopy(entry.printingSlug, entry.finish, qty) else null
        }

    override suspend fun currentWantedCopies(): List<WantedCopy> =
        socialDao.getWanted()
            .filter { it.quantity > 0 }
            .map { WantedCopy(it.printingSlug, it.finish, it.quantity) }

    private data class PrintingRef(val card: Card, val printing: Printing)

    private fun printingLookup(cards: List<CardWithPrintings>): Map<String, PrintingRef> {
        val map = HashMap<String, PrintingRef>()
        for (cwp in cards) {
            val card = cwp.toCard(imageResolver::imageUriForSlugs)
            for (printing in cwp.printings) {
                map[printing.slug] = PrintingRef(card, printing.toDomain())
            }
        }
        return map
    }
}
