package com.hayse.sorcery.feature.collection

import com.hayse.sorcery.feature.collection.data.repository.CollectionComputations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SurplusTest {

    @Test
    fun `ordinary a 5 exemplaires donne 1 de surplus`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Sea Serpent",
                rarity = "Ordinary",
                printings = listOf(CollectionFixtures.printing("slug-1", "Sea Serpent")),
            ),
        )
        val entries = listOf(CollectionFixtures.entry("slug-1", quantity = 5))
        val surplus = CollectionComputations.surplus(cards, entries)
        assertEquals(1, surplus.size)
        val s = surplus.single()
        assertEquals(5, s.owned)
        assertEquals(4, s.maxCopies)
        assertEquals(1, s.surplus)
    }

    @Test
    fun `au playset pas de surplus`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Sea Serpent",
                rarity = "Ordinary",
                printings = listOf(CollectionFixtures.printing("slug-1", "Sea Serpent")),
            ),
        )
        val entries = listOf(CollectionFixtures.entry("slug-1", quantity = 4))
        assertTrue(CollectionComputations.surplus(cards, entries).isEmpty())
    }

    @Test
    fun `surplus additionne les impressions et finishes`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Unique One",
                rarity = "Unique",
                printings = listOf(
                    CollectionFixtures.printing("slug-std", "Unique One", finish = "Standard"),
                    CollectionFixtures.printing("slug-foil", "Unique One", finish = "Foil"),
                ),
            ),
        )
        val entries = listOf(
            CollectionFixtures.entry("slug-std", finish = "Standard", quantity = 1),
            CollectionFixtures.entry("slug-foil", finish = "Foil", quantity = 2),
        )
        val s = CollectionComputations.surplus(cards, entries).single()
        assertEquals(3, s.owned)
        assertEquals(1, s.maxCopies)
        assertEquals(2, s.surplus)
    }

    @Test
    fun `rarete inconnue ignoree`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Sans Rarete",
                rarity = "Site",
                printings = listOf(CollectionFixtures.printing("slug-1", "Sans Rarete")),
            ),
        )
        val entries = listOf(CollectionFixtures.entry("slug-1", quantity = 99))
        assertTrue(CollectionComputations.surplus(cards, entries).isEmpty())
    }
}
