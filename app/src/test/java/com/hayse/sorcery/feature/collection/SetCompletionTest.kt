package com.hayse.sorcery.feature.collection

import com.hayse.sorcery.feature.collection.data.repository.CollectionComputations
import org.junit.Assert.assertEquals
import org.junit.Test

class SetCompletionTest {

    private val cards = listOf(
        CollectionFixtures.cardWith(
            "Sea Serpent",
            printings = listOf(CollectionFixtures.printing("ss-alpha", "Sea Serpent", setName = "Alpha")),
        ),
        CollectionFixtures.cardWith(
            "Foot Soldier",
            printings = listOf(
                CollectionFixtures.printing("fs-alpha", "Foot Soldier", setName = "Alpha"),
                CollectionFixtures.printing("fs-beta", "Foot Soldier", setName = "Beta"),
            ),
        ),
    )

    @Test
    fun `completion compte les cartes distinctes possedees par set`() {
        val entries = listOf(CollectionFixtures.entry("ss-alpha", quantity = 1))
        val completion = CollectionComputations.setCompletion(cards, entries)
        val alpha = completion.first { it.setName == "Alpha" }
        val beta = completion.first { it.setName == "Beta" }
        assertEquals(2, alpha.total)
        assertEquals(1, alpha.owned)
        assertEquals(1, beta.total)
        assertEquals(0, beta.owned)
    }

    @Test
    fun `sets tries par nom`() {
        val completion = CollectionComputations.setCompletion(cards, emptyList())
        assertEquals(listOf("Alpha", "Beta"), completion.map { it.setName })
    }

    @Test
    fun `manquants d'un set exclut les cartes possedees`() {
        val entries = listOf(CollectionFixtures.entry("ss-alpha", quantity = 1))
        val missing = CollectionComputations.missing(cards, entries, "Alpha")
        assertEquals(listOf("Foot Soldier"), missing.map { it.name })
    }

    @Test
    fun `carte possedee dans un autre set reste manquante ici`() {
        val entries = listOf(CollectionFixtures.entry("fs-beta", quantity = 1))
        val missing = CollectionComputations.missing(cards, entries, "Alpha")
        assertEquals(setOf("Foot Soldier", "Sea Serpent"), missing.map { it.name }.toSet())
    }
}
