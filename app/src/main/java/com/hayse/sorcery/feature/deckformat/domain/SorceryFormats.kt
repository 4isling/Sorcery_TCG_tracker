package com.hayse.sorcery.feature.deckformat.domain

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.deckformat.domain.model.CardCategory
import com.hayse.sorcery.feature.deckformat.domain.model.DeckFormat
import com.hayse.sorcery.feature.deckformat.domain.model.DeckZone
import com.hayse.sorcery.feature.deckformat.domain.model.ZoneRule

object SorceryFormats {

    private val standardZones = listOf(
        ZoneRule(DeckZone.SPELLBOOK, min = 60, max = null, allowedCategories = setOf(CardCategory.SPELL)),
        ZoneRule(DeckZone.ATLAS, min = 30, max = null, allowedCategories = setOf(CardCategory.SITE)),
        ZoneRule(DeckZone.COLLECTION, min = null, max = 10, allowedCategories = setOf(CardCategory.SPELL, CardCategory.SITE)),
    )

    val Constructed = DeckFormat(
        id = "constructed",
        displayName = "Constructed",
        zones = standardZones,
        copyLimits = mapOf(
            Rarity.Ordinary to 4,
            Rarity.Exceptional to 3,
            Rarity.Elite to 2,
            Rarity.Unique to 1,
        ),
    )

    val Poorcery = Constructed.copy(
        id = "poorcery",
        displayName = "Poorcery",
        copyLimits = mapOf(
            Rarity.Ordinary to 4,
            Rarity.Exceptional to 3,
        ),
    )

    val all: List<DeckFormat> = listOf(Constructed, Poorcery)

    fun fromId(id: String): DeckFormat = all.firstOrNull { it.id == id } ?: Constructed
}