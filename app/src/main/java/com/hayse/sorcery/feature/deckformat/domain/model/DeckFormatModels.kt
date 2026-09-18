package com.hayse.sorcery.feature.deckformat.domain.model

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card

enum class DeckZone { SPELLBOOK, ATLAS, COLLECTION }

enum class CardCategory { SPELL, SITE, AVATAR, TOKEN }

fun cardCategoryOf(card: Card): CardCategory = when (card.type) {
    "Avatar" -> CardCategory.AVATAR
    "Site" -> CardCategory.SITE
    "Token" -> CardCategory.TOKEN
    else -> CardCategory.SPELL
}

data class ZoneRule(
    val zone: DeckZone,
    val min: Int?,
    val max: Int?,
    val allowedCategories: Set<CardCategory>,
    val countsTowardCopyLimit: Boolean = true,
)

data class DeckFormat(
    val id: String,
    val displayName: String,
    val zones: List<ZoneRule>,
    val copyLimits: Map<Rarity, Int>,
    val bannedAvatars: Set<String> = emptySet(),
    val bannedCards: Set<String> = emptySet(),
    val unlimitedCopyCards: Set<String> = setOf("Grey Wolves", "Relentless Crowd"),
) {
    fun zoneRule(zone: DeckZone): ZoneRule? = zones.firstOrNull { it.zone == zone }
}

data class DeckEntry(
    val cardName: String,
    val zone: DeckZone,
    val quantity: Int,
)

data class Deck(
    val avatarName: String?,
    val entries: List<DeckEntry>,
)

data class PlayableCard(
    val cardName: String,
    val owned: Int,
    val playable: Int,
)