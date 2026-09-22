package com.hayse.sorcery.feature.deck

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckIssueMessage
import com.hayse.sorcery.feature.deck.domain.model.DeckSection
import com.hayse.sorcery.feature.deck.domain.model.DeckValidator
import com.hayse.sorcery.feature.deck.domain.model.missingTotal
import com.hayse.sorcery.feature.deckformat.card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Règles de la zone Collection (réserve de 10 cartes) dans le validateur de la feature deck. */
class DeckValidatorTest {

    private val format = DeckFormat.Constructed

    private fun spell(name: String, qty: Int, rarity: Rarity = Rarity.Ordinary, owned: Int = 0) =
        DeckEntry(card(name, "Minion", rarity), qty, owned)

    private fun site(name: String, qty: Int, owned: Int = 0) =
        DeckEntry(card(name, "Site", Rarity.Ordinary), qty, owned)

    private fun inCollection(name: String, qty: Int, type: String = "Minion", rarity: Rarity = Rarity.Ordinary) =
        DeckEntry(card(name, type, rarity), qty, owned = 0, section = DeckSection.Collection)

    /** Deck principal légal : 1 avatar, 60 sorts, 30 sites. */
    private fun legalMain(): List<DeckEntry> = buildList {
        add(DeckEntry(card("Sorcerer", "Avatar", null), 1, 0))
        repeat(15) { add(spell("Spell $it", 4)) }
        repeat(7) { add(site("Site $it", 4)) }
        add(site("Site 7", 2))
    }

    @Test
    fun `deck principal legal sans collection`() {
        val result = DeckValidator.validate(format, legalMain())
        assertTrue(result.issues.toString(), result.isLegal)
    }

    @Test
    fun `la collection ne compte ni dans le grimoire ni dans l atlas`() {
        val entries = legalMain() + inCollection("Extra", 4) + inCollection("Extra Site", 2, type = "Site")
        val result = DeckValidator.validate(format, entries)
        assertTrue(result.issues.toString(), result.isLegal)
    }

    @Test
    fun `collection au dela de 10 cartes`() {
        val entries = legalMain() + inCollection("A", 4) + inCollection("B", 4) + inCollection("C", 3)
        val result = DeckValidator.validate(format, entries)
        assertFalse(result.isLegal)
        val issue = result.issues.single { it.message is DeckIssueMessage.CollectionTooLarge }
        assertEquals(DeckIssueMessage.CollectionTooLarge(11, 10), issue.message)
        assertEquals(DeckSection.Collection, issue.section)
    }

    @Test
    fun `les copies en collection s ajoutent a celles du deck principal`() {
        val entries = legalMain() + inCollection("Spell 0", 1)
        val result = DeckValidator.validate(format, entries)
        assertFalse(result.isLegal)
        assertTrue(DeckIssueMessage.TooManyCopies("Spell 0", 5, 4) in result.issues.map { it.message })
    }

    @Test
    fun `une unique en collection respecte sa propre limite`() {
        val entries = legalMain() + inCollection("Relic", 1, rarity = Rarity.Unique)
        assertTrue(DeckValidator.validate(format, entries).isLegal)
        val tooMany = legalMain() + inCollection("Relic", 2, rarity = Rarity.Unique)
        assertTrue(DeckIssueMessage.TooManyCopies("Relic", 2, 1) in DeckValidator.validate(format, tooMany).issues.map { it.message })
    }

    @Test
    fun `la rarete interdite s applique aussi a la collection`() {
        val entries = legalMain() + inCollection("Dragon", 1, rarity = Rarity.Elite)
        val result = DeckValidator.validate(DeckFormat.Poorcery, entries)
        assertTrue(DeckIssueMessage.BannedRarity("Dragon", "Elite") in result.issues.map { it.message })
    }

    @Test
    fun `les manquantes partagent la possession entre les zones`() {
        val entries = listOf(
            spell("Bury", 4, owned = 5),
            DeckEntry(card("Bury", "Minion", Rarity.Ordinary), 2, owned = 5, section = DeckSection.Collection),
        )
        // 6 copies demandées, 5 possédées : il en manque 1 au total (et non 0 en comptant zone par zone).
        assertEquals(1, entries.missingTotal())
    }
}
