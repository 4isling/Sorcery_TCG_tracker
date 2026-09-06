package com.hayse.sorcery.feature.collection.data.repository

import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.elementGroupOf
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.local.model.PrintingKey
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.CuriosaRow
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard

/**
 * Logique pure de la collection (import/matching, complétion, surplus, manquants, items).
 * Séparée du repository pour être testée en JVM sans Room.
 */
object CollectionComputations {

    private data class MatchKey(val name: String, val set: String, val finish: String, val product: String)

    /** Plan d'import : upserts à appliquer + comptes pour le rapport. */
    data class ImportPlan(
        val upserts: List<CollectionEntryEntity>,
        val matched: Int,
        val unmatched: List<CuriosaRow>,
        val ambiguous: List<CuriosaRow>,
    )

    /** Associe chaque ligne CSV à une impression via (nom, set, finish, product). Collision -> 1er slug + signalé. */
    fun matchImport(rows: List<CuriosaRow>, keys: List<PrintingKey>): ImportPlan {
        val index = keys.groupBy { MatchKey(it.cardName, it.setName, it.finish, it.product) }
        val upserts = mutableListOf<CollectionEntryEntity>()
        var matched = 0
        val unmatched = mutableListOf<CuriosaRow>()
        val ambiguous = mutableListOf<CuriosaRow>()
        for (row in rows) {
            val hit = index[MatchKey(row.cardName, row.set, row.finish, row.product)]
            if (hit.isNullOrEmpty()) {
                unmatched += row
                continue
            }
            if (hit.size > 1) ambiguous += row
            upserts += CollectionEntryEntity(hit.first().slug, row.finish, row.quantity)
            matched++
        }
        return ImportPlan(upserts, matched, unmatched, ambiguous)
    }

    /** Quantités possédées par slug (>0). */
    private fun ownedBySlug(entries: List<CollectionEntryEntity>): Map<String, List<CollectionEntryEntity>> =
        entries.filter { it.quantity > 0 }.groupBy { it.printingSlug }

    /**
     * Une entrée par (carte, set d'impression) : la carte est dupliquée sous chaque set où elle
     * possède une impression. Les copies et le total sont propres au set. Filtrée par possession,
     * groupe d'élément et set optionnel.
     */
    fun collectionEntries(
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        ownership: Ownership,
        elementGroup: ElementGroup?,
        setName: String?,
        imageUriForSlugs: (List<String>) -> String? = { null },
    ): List<SetEntry<CollectionItem>> {
        val owned = ownedBySlug(entries)
        val result = mutableListOf<SetEntry<CollectionItem>>()
        for (cwp in cards) {
            val card = cwp.toCard(imageUriForSlugs)
            if (elementGroup != null && elementGroupOf(card.elements) != elementGroup) continue
            val maxCopies = card.rarity?.maxCopies ?: Int.MAX_VALUE
            val printingsBySet = cwp.printings.groupBy { it.setName }
            val sets = if (setName != null) listOf(setName) else printingsBySet.keys.toList()
            for (set in sets) {
                val prts = printingsBySet[set].orEmpty()
                if (prts.isEmpty()) continue
                val copies = prts.flatMap { owned[it.slug].orEmpty() }
                    .map { OwnedCopy(it.printingSlug, it.finish, it.quantity) }
                val total = copies.sumOf { it.quantity }
                val keep = when (ownership) {
                    Ownership.All -> true
                    Ownership.Owned -> total > 0
                    Ownership.Missing -> total == 0
                    Ownership.Surplus -> total > maxCopies
                }
                if (keep) result += SetEntry(set, card, CollectionItem(card, copies, total))
            }
        }
        return result
    }

    fun setCompletion(
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
    ): List<SetCompletion> {
        val ownedSlugs = entries.filter { it.quantity > 0 }.map { it.printingSlug }.toSet()
        val total = HashMap<String, Int>()
        val owned = HashMap<String, Int>()
        for (cwp in cards) {
            val bySet = cwp.printings.groupBy { it.setName }
            for ((set, prts) in bySet) {
                total[set] = (total[set] ?: 0) + 1
                if (prts.any { it.slug in ownedSlugs }) owned[set] = (owned[set] ?: 0) + 1
            }
        }
        return total.keys.sorted().map { SetCompletion(it, owned[it] ?: 0, total.getValue(it)) }
    }

    fun surplus(
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        imageUriForSlugs: (List<String>) -> String? = { null },
    ): List<SurplusCard> {
        val owned = ownedBySlug(entries)
        return cards.mapNotNull { cwp ->
            val card = cwp.toCard(imageUriForSlugs)
            val max = card.rarity?.maxCopies ?: return@mapNotNull null
            val totalOwned = cwp.printings.sumOf { p -> owned[p.slug].orEmpty().sumOf { it.quantity } }
            val over = totalOwned - max
            if (over > 0) SurplusCard(card, totalOwned, max, over) else null
        }.sortedByDescending { it.surplus }
    }

    fun missing(
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        setName: String,
        imageUriForSlugs: (List<String>) -> String? = { null },
    ): List<Card> {
        val ownedSlugs = entries.filter { it.quantity > 0 }.map { it.printingSlug }.toSet()
        return cards.filter { cwp ->
            val inSet = cwp.printings.filter { it.setName == setName }
            inSet.isNotEmpty() && inSet.none { it.slug in ownedSlugs }
        }.map { it.toCard(imageUriForSlugs) }.sortedBy { it.name }
    }
}
