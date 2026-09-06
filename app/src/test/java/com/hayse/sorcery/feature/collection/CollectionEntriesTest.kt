package com.hayse.sorcery.feature.collection

import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.feature.collection.data.repository.CollectionComputations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionEntriesTest {

    private fun entries(
        cards: List<com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings>,
        owned: List<com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity>,
        ownership: Ownership = Ownership.Owned,
        elementGroup: ElementGroup? = null,
        setName: String? = null,
    ) = CollectionComputations.collectionEntries(cards, owned, ownership, elementGroup, setName)

    @Test
    fun `une carte multi-set produit une entree par set d'impression`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Rubble",
                printings = listOf(
                    CollectionFixtures.printing("rubble-beta", "Rubble", setName = "Beta"),
                    CollectionFixtures.printing("rubble-gothic", "Rubble", setName = "Gothic"),
                ),
            ),
        )
        val owned = listOf(
            CollectionFixtures.entry("rubble-beta", quantity = 1),
            CollectionFixtures.entry("rubble-gothic", quantity = 2),
        )
        val result = entries(cards, owned)
        assertEquals(setOf("Beta", "Gothic"), result.map { it.setName }.toSet())
        assertEquals(1, result.single { it.setName == "Beta" }.payload.totalQuantity)
        assertEquals(2, result.single { it.setName == "Gothic" }.payload.totalQuantity)
    }

    @Test
    fun `les copies d'une entree sont propres au set`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Rubble",
                printings = listOf(
                    CollectionFixtures.printing("rubble-beta", "Rubble", setName = "Beta"),
                    CollectionFixtures.printing("rubble-gothic", "Rubble", setName = "Gothic"),
                ),
            ),
        )
        val owned = listOf(
            CollectionFixtures.entry("rubble-beta", quantity = 1),
            CollectionFixtures.entry("rubble-gothic", quantity = 2),
        )
        val beta = entries(cards, owned).single { it.setName == "Beta" }
        assertEquals(listOf("rubble-beta"), beta.payload.copies.map { it.slug })
    }

    @Test
    fun `ownership Owned exclut un set sans copie possedee`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Rubble",
                printings = listOf(
                    CollectionFixtures.printing("rubble-beta", "Rubble", setName = "Beta"),
                    CollectionFixtures.printing("rubble-gothic", "Rubble", setName = "Gothic"),
                ),
            ),
        )
        val owned = listOf(CollectionFixtures.entry("rubble-beta", quantity = 1))
        val result = entries(cards, owned, ownership = Ownership.Owned)
        assertEquals(listOf("Beta"), result.map { it.setName })
    }

    @Test
    fun `ownership Missing garde les sets sans copie`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Rubble",
                printings = listOf(
                    CollectionFixtures.printing("rubble-beta", "Rubble", setName = "Beta"),
                    CollectionFixtures.printing("rubble-gothic", "Rubble", setName = "Gothic"),
                ),
            ),
        )
        val owned = listOf(CollectionFixtures.entry("rubble-beta", quantity = 1))
        val result = entries(cards, owned, ownership = Ownership.Missing)
        assertEquals(listOf("Gothic"), result.map { it.setName })
    }

    @Test
    fun `le filtre elementGroup Neutral ne garde que les cartes sans element`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Site",
                elements = "",
                printings = listOf(CollectionFixtures.printing("site", "Site")),
            ),
            CollectionFixtures.cardWith(
                "Fireball",
                elements = "Fire",
                printings = listOf(CollectionFixtures.printing("fireball", "Fireball")),
            ),
        )
        val owned = listOf(
            CollectionFixtures.entry("site", quantity = 1),
            CollectionFixtures.entry("fireball", quantity = 1),
        )
        val result = entries(cards, owned, elementGroup = ElementGroup.Neutral)
        assertEquals(listOf("Site"), result.map { it.payload.card.name })
    }

    @Test
    fun `un setName filtre ne produit que ce set`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Rubble",
                printings = listOf(
                    CollectionFixtures.printing("rubble-beta", "Rubble", setName = "Beta"),
                    CollectionFixtures.printing("rubble-gothic", "Rubble", setName = "Gothic"),
                ),
            ),
        )
        val owned = listOf(
            CollectionFixtures.entry("rubble-beta", quantity = 1),
            CollectionFixtures.entry("rubble-gothic", quantity = 1),
        )
        val result = entries(cards, owned, setName = "Gothic")
        assertEquals(listOf("Gothic"), result.map { it.setName })
    }

    @Test
    fun `ownership All conserve tous les sets d'impression`() {
        val cards = listOf(
            CollectionFixtures.cardWith(
                "Rubble",
                printings = listOf(
                    CollectionFixtures.printing("rubble-beta", "Rubble", setName = "Beta"),
                    CollectionFixtures.printing("rubble-gothic", "Rubble", setName = "Gothic"),
                ),
            ),
        )
        val result = entries(cards, emptyList(), ownership = Ownership.All)
        assertTrue(result.all { it.payload.totalQuantity == 0 })
        assertEquals(setOf("Beta", "Gothic"), result.map { it.setName }.toSet())
    }
}
