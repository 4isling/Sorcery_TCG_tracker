package com.hayse.sorcery.feature.social.data.repository

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.cards.data.repository.toDomain
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.Printing
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.social.data.local.dao.SocialDao
import com.hayse.sorcery.feature.social.data.local.entity.SavedTradeEntity
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.SavedTradeItem
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SavedTradeRepositoryImpl(
    private val socialDao: SocialDao,
    private val cardDao: CardDao,
    private val imageUri: (List<String>) -> String?,
    private val collectionRepository: CollectionRepository,
) : SavedTradeRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val entrySerializer = ListSerializer(PayloadEntry.serializer())

    private fun observeAllCards() = cardDao.observeCards(null, null, null, null, null)

    override fun observeSavedTrades(): Flow<List<SavedTradeItem>> =
        combine(socialDao.observeSavedTrades(), observeAllCards()) { trades, cards ->
            val lookup = printingLookup(cards)
            trades.map { entity ->
                SavedTradeItem(
                    id = entity.id,
                    peerPseudo = entity.peerPseudo,
                    createdAt = entity.createdAt,
                    iGive = resolveLines(entity.giveJson, lookup),
                    iReceive = resolveLines(entity.receiveJson, lookup),
                    done = entity.done,
                )
            }
        }

    override suspend fun saveTrade(peerPseudo: String, iGive: List<MatchLine>, iReceive: List<MatchLine>) {
        socialDao.insertSavedTrade(
            SavedTradeEntity(
                peerPseudo = peerPseudo,
                createdAt = System.currentTimeMillis(),
                giveJson = encode(iGive),
                receiveJson = encode(iReceive),
            ),
        )
    }

    override suspend fun deleteTrade(id: Long) = socialDao.deleteSavedTrade(id)

    override suspend fun applyTrade(id: Long) {
        val entity = socialDao.getSavedTrade(id) ?: return
        decode(entity.giveJson).forEach { collectionRepository.adjustQuantity(it.slug, it.finish, -it.qty) }
        decode(entity.receiveJson).forEach { collectionRepository.adjustQuantity(it.slug, it.finish, it.qty) }
        socialDao.setSavedTradeDone(id, true)
    }

    private fun encode(lines: List<MatchLine>): String =
        json.encodeToString(entrySerializer, lines.map { PayloadEntry(it.slug, it.finish, it.quantity) })

    private fun decode(raw: String): List<PayloadEntry> =
        runCatching { json.decodeFromString(entrySerializer, raw) }.getOrDefault(emptyList())

    private fun resolveLines(raw: String, lookup: Map<String, PrintingRef>): List<TradeCardLine> =
        decode(raw).mapNotNull { entry ->
            val ref = lookup[entry.slug] ?: return@mapNotNull null
            TradeCardLine(ref.card, ref.printing, entry.qty)
        }

    private data class PrintingRef(val card: Card, val printing: Printing)

    private fun printingLookup(cards: List<CardWithPrintings>): Map<String, PrintingRef> {
        val map = HashMap<String, PrintingRef>()
        for (cwp in cards) {
            val card = cwp.toCard(imageUri)
            for (printing in cwp.printings) {
                map[printing.slug] = PrintingRef(card, printing.toDomain())
            }
        }
        return map
    }
}
