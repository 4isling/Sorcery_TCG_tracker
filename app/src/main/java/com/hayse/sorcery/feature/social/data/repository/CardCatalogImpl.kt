package com.hayse.sorcery.feature.social.data.repository

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.repository.CardImageResolver
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.cards.data.repository.toDomain
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.Printing
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.domain.repository.CardCatalog
import kotlinx.coroutines.flow.first

class CardCatalogImpl(
    private val cardDao: CardDao,
    private val imageResolver: CardImageResolver,
) : CardCatalog {

    override suspend fun resolve(lines: List<MatchLine>): List<TradeCardLine> {
        if (lines.isEmpty()) return emptyList()
        val cards = cardDao.observeCards(null, null, null, null, null).first()
        val lookup = printingLookup(cards)
        return lines.mapNotNull { line ->
            val ref = lookup[line.slug] ?: return@mapNotNull null
            TradeCardLine(ref.card, ref.printing, line.quantity)
        }
    }

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
