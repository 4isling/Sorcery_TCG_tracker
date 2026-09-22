package com.hayse.sorcery.feature.statistics.data.repository

import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.elementGroupOf
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.cards.domain.model.setRank
import com.hayse.sorcery.feature.statistics.domain.model.CollectionStats
import com.hayse.sorcery.feature.statistics.domain.model.ElementCount
import com.hayse.sorcery.feature.statistics.domain.model.SEALED_CONFIGS
import com.hayse.sorcery.feature.statistics.domain.model.SetStat

/**
 * Logique pure des statistiques de collection. Séparée du repository pour être testée en JVM
 * sans Room. Une entrée de collection porte sur une impression (slug), donc son set est celui
 * de l'impression ; l'élément est en revanche celui de la carte.
 */
object StatisticsComputations {

    fun compute(
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
    ): CollectionStats {
        val owned = entries.filter { it.quantity > 0 }
        if (owned.isEmpty()) return CollectionStats()

        val slugToSet = HashMap<String, String>()
        val slugToGroup = HashMap<String, ElementGroup>()
        val slugToName = HashMap<String, String>()
        for (cwp in cards) {
            val card = cwp.toCard()
            val group = elementGroupOf(card.elements)
            for (printing in cwp.printings) {
                slugToSet[printing.slug] = printing.setName
                slugToGroup[printing.slug] = group
                slugToName[printing.slug] = card.name
            }
        }

        var total = 0
        val bySetCount = HashMap<String, Int>()
        val byGroupCount = HashMap<ElementGroup, Int>()
        val ownedNames = HashSet<String>()
        for (entry in owned) {
            val qty = entry.quantity
            total += qty
            slugToSet[entry.printingSlug]?.let { bySetCount[it] = (bySetCount[it] ?: 0) + qty }
            slugToGroup[entry.printingSlug]?.let { byGroupCount[it] = (byGroupCount[it] ?: 0) + qty }
            slugToName[entry.printingSlug]?.let { ownedNames += it }
        }

        val byElement = ElementGroup.entries
            .mapNotNull { group -> byGroupCount[group]?.let { ElementCount(group, it) } }

        val bySet = bySetCount.entries.map { (set, count) ->
            val cfg = SEALED_CONFIGS[set]
            val boosters = cfg?.cardsPerBooster?.let { count.toDouble() / it }
            val displays = if (boosters != null && cfg.boostersPerDisplay != null) {
                boosters / cfg.boostersPerDisplay
            } else {
                null
            }
            val boxes = cfg?.cardsPerBox?.let { count.toDouble() / it }
            SetStat(set, count, boosters, displays, boxes)
        }.sortedWith(compareBy({ setRank(it.setName) }, { it.setName.lowercase() }))

        return CollectionStats(
            totalCards = total,
            distinctCards = ownedNames.size,
            byElement = byElement,
            bySet = bySet,
            totalBoosters = bySet.sumOf { it.boosters ?: 0.0 },
            totalDisplays = bySet.sumOf { it.displays ?: 0.0 },
        )
    }
}
