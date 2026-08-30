package com.hayse.sorcery.feature.deck.data.repository

import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.repository.toCard
import com.hayse.sorcery.feature.deck.data.local.entity.DeckCardEntity
import com.hayse.sorcery.feature.deck.data.local.entity.DeckEntity
import com.hayse.sorcery.feature.deck.domain.model.DeckDetail
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSection
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.model.DeckValidator
import com.hayse.sorcery.feature.deck.domain.model.deckSectionOf

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
        val deckEntries = deckCards.mapNotNull { dc ->
            val card = cardByName[dc.cardName] ?: return@mapNotNull null
            DeckEntry(card = card, quantity = dc.quantity, owned = ownedByCard[dc.cardName] ?: 0)
        }.sortedBy { it.card.name }
        return DeckDetail(
            id = deck.id,
            name = deck.name,
            format = DeckFormat.fromId(deck.format),
            entries = deckEntries,
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
        val byDeck = allDeckCards.groupBy { it.deckId }
        return decks.map { deck ->
            val format = DeckFormat.fromId(deck.format)
            val deckEntries = byDeck[deck.id].orEmpty().mapNotNull { dc ->
                val card = cardByName[dc.cardName] ?: return@mapNotNull null
                DeckEntry(card = card, quantity = dc.quantity, owned = ownedByCard[dc.cardName] ?: 0)
            }
            val avatar = deckEntries.firstOrNull { deckSectionOf(it.card.type) == DeckSection.Avatar }
            DeckSummary(
                id = deck.id,
                name = deck.name,
                format = format,
                avatarName = avatar?.card?.name,
                avatarImageUri = avatar?.card?.imageUri,
                spellbookCount = deckEntries.filter { deckSectionOf(it.card.type) == DeckSection.Spellbook }.sumOf { it.quantity },
                atlasCount = deckEntries.filter { deckSectionOf(it.card.type) == DeckSection.Atlas }.sumOf { it.quantity },
                isLegal = DeckValidator.validate(format, deckEntries).isLegal,
                updatedAt = deck.updatedAt,
            )
        }
    }

    fun catalog(
        deckCards: List<DeckCardEntity>,
        cards: List<CardWithPrintings>,
        entries: List<CollectionEntryEntity>,
        ownership: Ownership,
        section: DeckSection?,
        imageUriForSlugs: (List<String>) -> String?,
    ): List<DeckEntry> {
        val ownedByCard = ownedByCard(cards, entries)
        val inDeck = deckCards.associate { it.cardName to it.quantity }
        return cards.asSequence()
            .map { cwp ->
                val card = cwp.toCard(imageUriForSlugs)
                DeckEntry(card = card, quantity = inDeck[card.name] ?: 0, owned = ownedByCard[card.name] ?: 0)
            }
            .filter { section == null || deckSectionOf(it.card.type) == section }
            .filter { keepByOwnership(it, ownership) }
            .toList()
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
