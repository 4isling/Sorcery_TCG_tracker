package com.hayse.sorcery.feature.deckformat.domain

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.deckformat.domain.model.CardCategory
import com.hayse.sorcery.feature.deckformat.domain.model.DeckFormat
import com.hayse.sorcery.feature.deckformat.domain.model.PlayableCard
import com.hayse.sorcery.feature.deckformat.domain.model.cardCategoryOf

object PlayableCards {

    fun compute(owned: List<Pair<Card, Int>>, format: DeckFormat): List<PlayableCard> =
        owned
            .groupBy { it.first.name }
            .mapNotNull { (name, rows) ->
                val card = rows.first().first
                val category = cardCategoryOf(card)
                if (category == CardCategory.AVATAR || category == CardCategory.TOKEN) return@mapNotNull null

                val rarity = card.rarity
                val limit = rarity?.let { format.copyLimits[it] }
                if (rarity == null || limit == null || name in format.bannedCards) return@mapNotNull null

                val ownedTotal = rows.sumOf { it.second }
                val playable = if (name in format.unlimitedCopyCards) ownedTotal else minOf(ownedTotal, limit)
                PlayableCard(cardName = name, owned = ownedTotal, playable = playable)
            }
            .sortedBy { it.cardName }
}