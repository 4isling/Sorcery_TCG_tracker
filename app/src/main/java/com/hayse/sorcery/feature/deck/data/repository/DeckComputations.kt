package com.hayse.sorcery.feature.deck.data.repository

import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.deck.data.local.entity.DeckCardEntity
import com.hayse.sorcery.feature.deck.data.local.entity.DeckEntity
import com.hayse.sorcery.feature.deck.domain.model.DeckDetail
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSection
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.model.DeckValidator
import com.hayse.sorcery.feature.deck.domain.model.deckSectionOf
import com.hayse.sorcery.feature.deck.domain.model.isCollectionEligible

/** Assemble les entités deck + catalogue + collection en modèles de domaine enrichis de la possession. */
object DeckComputations {

    fun deckDetail(
        deck: DeckEntity,
        deckCards: List<DeckCardEntity>,
        allCards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        imageUriForSlugs: (List<String>) -> String?,
    ): DeckDetail {
        val ownedByCard = ownedByCard(allCards, entries)
        val cardByName = allCards.associate { it.card.name to it.toCard(imageUriForSlugs) }
        val setByName = allCards.associate { it.card.name to it.printings.firstOrNull()?.setName }
        val deckEntries = deckEntries(deckCards, cardByName, ownedByCard).sortedBy { it.card.name }
        val avatarName = deckEntries.firstOrNull { it.section == DeckSection.Avatar }?.card?.name
        return DeckDetail(
            id = deck.id,
            name = deck.name,
            format = DeckFormat.fromId(deck.format),
            entries = deckEntries,
            avatarSetName = avatarName?.let { setByName[it] },
        )
    }

    fun deckSummaries(
        decks: List<DeckEntity>,
        allDeckCards: List<DeckCardEntity>,
        allCards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        imageUriForSlugs: (List<String>) -> String?,
    ): List<DeckSummary> {
        val ownedByCard = ownedByCard(allCards, entries)
        val cardByName = allCards.associate { it.card.name to it.toCard(imageUriForSlugs) }
        val setByName = allCards.associate { it.card.name to it.printings.firstOrNull()?.setName }
        val byDeck = allDeckCards.groupBy { it.deckId }
        return decks.map { deck ->
            val format = DeckFormat.fromId(deck.format)
            val deckEntries = deckEntries(byDeck[deck.id].orEmpty(), cardByName, ownedByCard)
            val avatar = deckEntries.firstOrNull { it.section == DeckSection.Avatar }
            fun countIn(section: DeckSection) = deckEntries.filter { it.section == section }.sumOf { it.quantity }
            DeckSummary(
                id = deck.id,
                name = deck.name,
                format = format,
                avatarName = avatar?.card?.name,
                avatarSetName = avatar?.card?.name?.let { setByName[it] },
                avatarImageUri = avatar?.card?.imageUri,
                spellbookCount = countIn(DeckSection.Spellbook),
                atlasCount = countIn(DeckSection.Atlas),
                collectionCount = countIn(DeckSection.Collection),
                isLegal = DeckValidator.validate(format, deckEntries).isLegal,
                updatedAt = deck.updatedAt,
            )
        }
    }

    /**
     * Catalogue avec la quantité dans le deck pour chaque carte. Quand [section] est la Collection,
     * seules les cartes éligibles (ni Avatar ni jeton) apparaissent, avec leur quantité en Collection ;
     * sinon la quantité est celle du deck principal et la zone découle du type de la carte.
     */
    fun catalog(
        deckCards: List<DeckCardEntity>,
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        ownership: Ownership,
        section: DeckSection?,
        imageUriForSlugs: (List<String>) -> String?,
    ): List<DeckEntry> {
        val ownedByCard = ownedByCard(cards, entries)
        val byName = deckCards.associateBy { it.cardName }
        val forCollection = section == DeckSection.Collection
        return cards.asSequence()
            .map { cwp ->
                val card = cwp.toCard(imageUriForSlugs)
                val stored = byName[card.name]
                DeckEntry(
                    card = card,
                    quantity = (if (forCollection) stored?.collectionQuantity else stored?.quantity) ?: 0,
                    owned = ownedByCard[card.name] ?: 0,
                    section = if (forCollection) DeckSection.Collection else deckSectionOf(card.type),
                )
            }
            .filter {
                when {
                    forCollection -> isCollectionEligible(it.card.type)
                    section == null -> true
                    else -> it.section == section
                }
            }
            .filter { keepByOwnership(it, ownership) }
            .toList()
    }

    /** Une entité peut produire deux entrées : deck principal (zone déduite du type) et Collection. */
    private fun deckEntries(
        deckCards: List<DeckCardEntity>,
        cardByName: Map<String, Card>,
        ownedByCard: Map<String, Int>,
    ): List<DeckEntry> = deckCards.flatMap { dc ->
        val card = cardByName[dc.cardName] ?: return@flatMap emptyList()
        val owned = ownedByCard[dc.cardName] ?: 0
        buildList {
            if (dc.quantity > 0) add(DeckEntry(card = card, quantity = dc.quantity, owned = owned))
            if (dc.collectionQuantity > 0) {
                add(DeckEntry(card = card, quantity = dc.collectionQuantity, owned = owned, section = DeckSection.Collection))
            }
        }
    }

    private fun keepByOwnership(entry: DeckEntry, ownership: Ownership): Boolean = when (ownership) {
        Ownership.All -> true
        Ownership.Owned -> entry.owned > 0
        Ownership.Missing -> entry.owned == 0
        Ownership.Surplus -> entry.owned > entry.quantity
    }

    private fun ownedByCard(
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
    ): Map<String, Int> {
        val quantityBySlug = entries.associate { it.printingSlug to it.quantity }
        return cards.associate { cwp ->
            cwp.card.name to cwp.printings.sumOf { quantityBySlug[it.slug] ?: 0 }
        }
    }
}
