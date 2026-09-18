package com.hayse.sorcery.feature.deckformat

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.deckformat.domain.DeckValidator
import com.hayse.sorcery.feature.deckformat.domain.SorceryFormats
import com.hayse.sorcery.feature.deckformat.domain.model.Deck
import com.hayse.sorcery.feature.deckformat.domain.model.DeckEntry
import com.hayse.sorcery.feature.deckformat.domain.model.DeckFormat
import com.hayse.sorcery.feature.deckformat.domain.model.DeckIssue
import com.hayse.sorcery.feature.deckformat.domain.model.DeckZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckValidatorTest {

    private val constructed = SorceryFormats.Constructed
    private val poorcery = SorceryFormats.Poorcery

    private fun validate(deck: Deck, format: DeckFormat, cards: Map<String, Card>) =
        DeckValidator.validate(deck, format, cards)

    @Test
    fun `reference deck is legal in poorcery`() {
        val result = validate(referenceDeck(), poorcery, referenceCards())
        assertTrue(result.issues.toString(), result.isLegal)
    }

    @Test
    fun `reference deck is legal in constructed`() {
        val result = validate(referenceDeck(), constructed, referenceCards())
        assertTrue(result.issues.toString(), result.isLegal)
    }

    @Test
    fun `spellbook below minimum`() {
        val deck = referenceDeck().let { base ->
            base.copy(entries = base.entries.map {
                if (it.cardName == "Toolbox") it.copy(quantity = 1) else it
            })
        }
        val result = validate(deck, poorcery, referenceCards())
        assertTrue(DeckIssue.ZoneTooSmall(DeckZone.SPELLBOOK, 59, 60) in result.issues)
    }

    @Test
    fun `collection above maximum`() {
        val deck = referenceDeck().let { base ->
            base.copy(entries = base.entries + DeckEntry("Extra Site", DeckZone.COLLECTION, 1))
        }
        val cards = referenceCards() + ("Extra Site" to card("Extra Site", "Site", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.ZoneTooLarge(DeckZone.COLLECTION, 11, 10) in result.issues)
    }

    @Test
    fun `elite card is banned in poorcery but not flagged for copies`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Elite Guy", DeckZone.SPELLBOOK, 2)))
        val cards = mapOf("Elite Guy" to card("Elite Guy", "Minion", Rarity.Elite))
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.RarityNotAllowed("Elite Guy", Rarity.Elite) in result.issues)
        assertFalse(result.issues.any { it is DeckIssue.TooManyCopies })
    }

    @Test
    fun `elite card is allowed in constructed`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Elite Guy", DeckZone.SPELLBOOK, 2)))
        val cards = mapOf("Elite Guy" to card("Elite Guy", "Minion", Rarity.Elite))
        val result = validate(deck, constructed, cards)
        assertFalse(result.issues.any { it is DeckIssue.RarityNotAllowed })
        assertFalse(result.issues.any { it is DeckIssue.TooManyCopies })
    }

    @Test
    fun `four exceptional copies exceed the limit`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Cave-In", DeckZone.SPELLBOOK, 4)))
        val cards = mapOf("Cave-In" to card("Cave-In", "Magic", Rarity.Exceptional))
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.TooManyCopies("Cave-In", 4, 3) in result.issues)
    }

    @Test
    fun `copies are counted across zones`() {
        val deck = Deck(
            "Sorcerer",
            listOf(
                DeckEntry("Bury", DeckZone.SPELLBOOK, 4),
                DeckEntry("Bury", DeckZone.COLLECTION, 1),
            ),
        )
        val cards = mapOf("Bury" to card("Bury", "Magic", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.TooManyCopies("Bury", 5, 4) in result.issues)
    }

    @Test
    fun `collection can be excluded from the copy limit`() {
        val format = poorcery.copy(
            zones = poorcery.zones.map {
                if (it.zone == DeckZone.COLLECTION) it.copy(countsTowardCopyLimit = false) else it
            },
        )
        val deck = Deck(
            "Sorcerer",
            listOf(
                DeckEntry("Bury", DeckZone.SPELLBOOK, 4),
                DeckEntry("Bury", DeckZone.COLLECTION, 1),
            ),
        )
        val cards = mapOf("Bury" to card("Bury", "Magic", Rarity.Ordinary))
        val result = validate(deck, format, cards)
        assertFalse(result.issues.any { it is DeckIssue.TooManyCopies })
    }

    @Test
    fun `unlimited copy cards ignore the limit`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Grey Wolves", DeckZone.SPELLBOOK, 12)))
        val cards = mapOf("Grey Wolves" to card("Grey Wolves", "Minion", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertFalse(result.issues.any { it is DeckIssue.TooManyCopies })
    }

    @Test
    fun `site in the spellbook is in the wrong zone`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Common Village", DeckZone.SPELLBOOK, 1)))
        val cards = mapOf("Common Village" to card("Common Village", "Site", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.WrongZone("Common Village", DeckZone.SPELLBOOK) in result.issues)
    }

    @Test
    fun `site in the collection is allowed`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Common Village", DeckZone.COLLECTION, 1)))
        val cards = mapOf("Common Village" to card("Common Village", "Site", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertFalse(result.issues.any { it is DeckIssue.WrongZone })
    }

    @Test
    fun `token in the spellbook is not a deck card`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Foot Soldier", DeckZone.SPELLBOOK, 1)))
        val cards = mapOf("Foot Soldier" to card("Foot Soldier", "Token", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.NotADeckCard("Foot Soldier") in result.issues)
    }

    @Test
    fun `missing avatar is reported`() {
        val deck = referenceDeck().copy(avatarName = null)
        val result = validate(deck, poorcery, referenceCards())
        assertTrue(DeckIssue.MissingAvatar in result.issues)
    }

    @Test
    fun `banned avatar is reported`() {
        val format = poorcery.copy(bannedAvatars = setOf("Sorcerer"))
        val result = validate(referenceDeck(), format, referenceCards())
        assertTrue(DeckIssue.BannedAvatar("Sorcerer") in result.issues)
    }

    @Test
    fun `avatar with null rarity is legal`() {
        val deck = Deck("Druid", listOf(DeckEntry("Bosk Troll", DeckZone.SPELLBOOK, 1)))
        val cards = mapOf("Bosk Troll" to card("Bosk Troll", "Minion", Rarity.Ordinary))
        val result = validate(deck, poorcery, cards)
        assertFalse(result.issues.any { it is DeckIssue.BannedAvatar || it == DeckIssue.MissingAvatar })
    }

    @Test
    fun `unknown card is reported`() {
        val deck = Deck("Sorcerer", listOf(DeckEntry("Nonexistent", DeckZone.SPELLBOOK, 1)))
        val result = validate(deck, poorcery, emptyMap())
        assertTrue(DeckIssue.UnknownCard("Nonexistent") in result.issues)
    }

    @Test
    fun `several issues are all returned`() {
        val deck = Deck(
            avatarName = null,
            entries = listOf(
                DeckEntry("Elite Guy", DeckZone.SPELLBOOK, 2),
                DeckEntry("Cave-In", DeckZone.SPELLBOOK, 4),
            ),
        )
        val cards = mapOf(
            "Elite Guy" to card("Elite Guy", "Minion", Rarity.Elite),
            "Cave-In" to card("Cave-In", "Magic", Rarity.Exceptional),
        )
        val result = validate(deck, poorcery, cards)
        assertTrue(DeckIssue.MissingAvatar in result.issues)
        assertTrue(DeckIssue.RarityNotAllowed("Elite Guy", Rarity.Elite) in result.issues)
        assertTrue(DeckIssue.TooManyCopies("Cave-In", 4, 3) in result.issues)
    }

    @Test
    fun `banned card is reported`() {
        val format = poorcery.copy(bannedCards = setOf("Cave-In"))
        val deck = Deck("Sorcerer", listOf(DeckEntry("Cave-In", DeckZone.SPELLBOOK, 1)))
        val cards = mapOf("Cave-In" to card("Cave-In", "Magic", Rarity.Ordinary))
        val result = validate(deck, format, cards)
        assertTrue(DeckIssue.BannedCard("Cave-In") in result.issues)
    }

    @Test
    fun `spellbook count ignores wrong-zone and unknown entries`() {
        val deck = Deck(
            "Sorcerer",
            listOf(DeckEntry("Common Village", DeckZone.ATLAS, 30)) +
                (1..60).map { DeckEntry("Spell$it", DeckZone.SPELLBOOK, 1) },
        )
        val cards = buildMap {
            put("Common Village", card("Common Village", "Site", Rarity.Ordinary))
            (1..60).forEach { put("Spell$it", card("Spell$it", "Minion", Rarity.Ordinary)) }
        }
        val result = validate(deck, poorcery, cards)
        assertEquals(result.issues.toString(), emptyList<DeckIssue>(), result.issues)
    }
}
