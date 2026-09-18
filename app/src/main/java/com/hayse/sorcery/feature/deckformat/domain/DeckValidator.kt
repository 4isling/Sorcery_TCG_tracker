package com.hayse.sorcery.feature.deckformat.domain

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.deckformat.domain.model.CardCategory
import com.hayse.sorcery.feature.deckformat.domain.model.Deck
import com.hayse.sorcery.feature.deckformat.domain.model.DeckEntry
import com.hayse.sorcery.feature.deckformat.domain.model.DeckFormat
import com.hayse.sorcery.feature.deckformat.domain.model.DeckIssue
import com.hayse.sorcery.feature.deckformat.domain.model.DeckValidation
import com.hayse.sorcery.feature.deckformat.domain.model.cardCategoryOf

object DeckValidator {

    private data class ResolvedEntry(val entry: DeckEntry, val card: Card) {
        val category: CardCategory get() = cardCategoryOf(card)
    }

    fun validate(deck: Deck, format: DeckFormat, cardsByName: Map<String, Card>): DeckValidation {
        val issues = mutableListOf<DeckIssue>()

        validateAvatar(deck, format, issues)

        val resolved = mutableListOf<ResolvedEntry>()
        deck.entries.forEach { entry ->
            val card = cardsByName[entry.cardName]
            when {
                card == null -> issues += DeckIssue.UnknownCard(entry.cardName)
                cardCategoryOf(card) == CardCategory.AVATAR || cardCategoryOf(card) == CardCategory.TOKEN ->
                    issues += DeckIssue.NotADeckCard(entry.cardName)
                else -> resolved += ResolvedEntry(entry, card)
            }
        }

        validateZoneSizes(format, resolved, issues)
        validateZones(format, resolved, issues)
        validateByCard(format, resolved, issues)

        return DeckValidation(issues)
    }

    private fun validateAvatar(deck: Deck, format: DeckFormat, issues: MutableList<DeckIssue>) {
        val name = deck.avatarName
        when {
            name == null -> issues += DeckIssue.MissingAvatar
            name in format.bannedAvatars -> issues += DeckIssue.BannedAvatar(name)
        }
    }

    private fun validateZoneSizes(
        format: DeckFormat,
        resolved: List<ResolvedEntry>,
        issues: MutableList<DeckIssue>,
    ) {
        format.zones.forEach { rule ->
            val count = resolved.filter { it.entry.zone == rule.zone }.sumOf { it.entry.quantity }
            if (rule.min != null && count < rule.min) {
                issues += DeckIssue.ZoneTooSmall(rule.zone, count, rule.min)
            }
            if (rule.max != null && count > rule.max) {
                issues += DeckIssue.ZoneTooLarge(rule.zone, count, rule.max)
            }
        }
    }

    private fun validateZones(
        format: DeckFormat,
        resolved: List<ResolvedEntry>,
        issues: MutableList<DeckIssue>,
    ) {
        resolved.forEach { resolvedEntry ->
            val rule = format.zoneRule(resolvedEntry.entry.zone) ?: return@forEach
            if (resolvedEntry.category !in rule.allowedCategories) {
                issues += DeckIssue.WrongZone(resolvedEntry.entry.cardName, resolvedEntry.entry.zone)
            }
        }
    }

    private fun validateByCard(
        format: DeckFormat,
        resolved: List<ResolvedEntry>,
        issues: MutableList<DeckIssue>,
    ) {
        resolved.groupBy { it.entry.cardName }.forEach { (name, entries) ->
            val card = entries.first().card
            val rarity = card.rarity
            val rarityAllowed = rarity != null && rarity in format.copyLimits

            if (rarity == null || !rarityAllowed) {
                if (rarity != null) issues += DeckIssue.RarityNotAllowed(name, rarity)
            }

            if (name in format.bannedCards) {
                issues += DeckIssue.BannedCard(name)
            }

            if (rarityAllowed && name !in format.unlimitedCopyCards) {
                val counted = entries
                    .filter { format.zoneRule(it.entry.zone)?.countsTowardCopyLimit ?: true }
                    .sumOf { it.entry.quantity }
                val limit = format.copyLimits.getValue(rarity!!)
                if (counted > limit) {
                    issues += DeckIssue.TooManyCopies(name, counted, limit)
                }
            }
        }
    }
}