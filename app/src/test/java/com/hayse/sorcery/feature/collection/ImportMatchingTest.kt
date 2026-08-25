package com.hayse.sorcery.feature.collection

import com.hayse.sorcery.feature.collection.data.repository.CollectionComputations
import com.hayse.sorcery.feature.collection.domain.model.CuriosaRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportMatchingTest {

    private fun row(
        name: String,
        set: String = "Alpha",
        finish: String = "Standard",
        product: String = "Booster",
        qty: Int = 1,
    ) = CuriosaRow(name, set, finish, product, qty)

    @Test
    fun `ligne matchee produit un upsert`() {
        val keys = listOf(CollectionFixtures.key("slug-1", "Sea Serpent"))
        val plan = CollectionComputations.matchImport(listOf(row("Sea Serpent", qty = 3)), keys)
        assertEquals(1, plan.matched)
        assertEquals(1, plan.upserts.size)
        val upsert = plan.upserts.single()
        assertEquals("slug-1", upsert.printingSlug)
        assertEquals("Standard", upsert.finish)
        assertEquals(3, upsert.quantity)
        assertTrue(plan.unmatched.isEmpty())
        assertTrue(plan.ambiguous.isEmpty())
    }

    @Test
    fun `ligne sans correspondance va dans unmatched`() {
        val keys = listOf(CollectionFixtures.key("slug-1", "Sea Serpent"))
        val plan = CollectionComputations.matchImport(listOf(row("Inconnu")), keys)
        assertEquals(0, plan.matched)
        assertTrue(plan.upserts.isEmpty())
        assertEquals(1, plan.unmatched.size)
    }

    @Test
    fun `collision prend le premier slug et signale ambigu`() {
        val keys = listOf(
            CollectionFixtures.key("slug-a", "Foot Soldier"),
            CollectionFixtures.key("slug-b", "Foot Soldier"),
        )
        val plan = CollectionComputations.matchImport(listOf(row("Foot Soldier", qty = 2)), keys)
        assertEquals(1, plan.matched)
        assertEquals("slug-a", plan.upserts.single().printingSlug)
        assertEquals(1, plan.ambiguous.size)
    }

    @Test
    fun `finish distingue deux impressions`() {
        val keys = listOf(
            CollectionFixtures.key("slug-std", "Sea Serpent", finish = "Standard"),
            CollectionFixtures.key("slug-foil", "Sea Serpent", finish = "Foil"),
        )
        val plan = CollectionComputations.matchImport(
            listOf(row("Sea Serpent", finish = "Foil", qty = 1)),
            keys,
        )
        assertEquals("slug-foil", plan.upserts.single().printingSlug)
        assertTrue(plan.ambiguous.isEmpty())
    }
}
