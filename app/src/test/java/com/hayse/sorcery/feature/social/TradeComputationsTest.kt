package com.hayse.sorcery.feature.social

import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.social.data.repository.TradeComputations
import com.hayse.sorcery.feature.social.domain.model.TradeableCopy
import com.hayse.sorcery.feature.social.domain.model.WantedCopy
import org.junit.Assert.assertEquals
import org.junit.Test

class TradeComputationsTest {

    @Test
    fun `tradeable ne liste que les impressions possedees`() {
        val owned = listOf(
            OwnedCopy("slug-a", "standard", 2),
            OwnedCopy("slug-b", "foil", 0),
        )
        val rows = TradeComputations.tradeableRows(owned, tradeable = emptyList())
        assertEquals(1, rows.size)
        assertEquals("slug-a", rows.single().slug)
    }

    @Test
    fun `quantite a echanger est plafonnee par le possede`() {
        val owned = listOf(OwnedCopy("slug-a", "standard", 2))
        val tradeable = listOf(TradeableCopy("slug-a", "standard", 5))
        val row = TradeComputations.tradeableRows(owned, tradeable).single()
        assertEquals(2, row.ownedQty)
        assertEquals(2, row.tradeableQty)
    }

    @Test
    fun `quantite a echanger conserve une valeur inferieure au possede`() {
        val owned = listOf(OwnedCopy("slug-a", "standard", 4))
        val tradeable = listOf(TradeableCopy("slug-a", "standard", 1))
        val row = TradeComputations.tradeableRows(owned, tradeable).single()
        assertEquals(1, row.tradeableQty)
    }

    @Test
    fun `impression possedee sans entree a echanger a une quantite nulle`() {
        val owned = listOf(OwnedCopy("slug-a", "standard", 3))
        val row = TradeComputations.tradeableRows(owned, tradeable = emptyList()).single()
        assertEquals(0, row.tradeableQty)
        assertEquals(3, row.ownedQty)
    }

    @Test
    fun `finish distinct est une ligne distincte`() {
        val owned = listOf(
            OwnedCopy("slug-a", "standard", 1),
            OwnedCopy("slug-a", "foil", 1),
        )
        val tradeable = listOf(TradeableCopy("slug-a", "foil", 1))
        val rows = TradeComputations.tradeableRows(owned, tradeable)
        assertEquals(2, rows.size)
        assertEquals(0, rows.first { it.finish == "standard" }.tradeableQty)
        assertEquals(1, rows.first { it.finish == "foil" }.tradeableQty)
    }

    @Test
    fun `tradeable est trie par slug puis finish`() {
        val owned = listOf(
            OwnedCopy("slug-b", "standard", 1),
            OwnedCopy("slug-a", "standard", 1),
            OwnedCopy("slug-a", "foil", 1),
        )
        val rows = TradeComputations.tradeableRows(owned, tradeable = emptyList())
        assertEquals(
            listOf("slug-a" to "foil", "slug-a" to "standard", "slug-b" to "standard"),
            rows.map { it.slug to it.finish },
        )
    }

    @Test
    fun `wanted conserve les quantites sans perte`() {
        val wanted = listOf(
            WantedCopy("slug-a", "standard", 2),
            WantedCopy("slug-b", "foil", 1),
        )
        val rows = TradeComputations.wantedRows(wanted)
        assertEquals(2, rows.size)
        assertEquals(2, rows.first { it.slug == "slug-a" }.wantedQty)
        assertEquals(1, rows.first { it.slug == "slug-b" }.wantedQty)
    }

    @Test
    fun `wanted ignore les quantites nulles`() {
        val wanted = listOf(
            WantedCopy("slug-a", "standard", 0),
            WantedCopy("slug-b", "foil", 3),
        )
        val rows = TradeComputations.wantedRows(wanted)
        assertEquals(1, rows.size)
        assertEquals("slug-b", rows.single().slug)
    }

    @Test
    fun `wanted est trie par slug puis finish`() {
        val wanted = listOf(
            WantedCopy("slug-b", "standard", 1),
            WantedCopy("slug-a", "standard", 1),
            WantedCopy("slug-a", "foil", 1),
        )
        val rows = TradeComputations.wantedRows(wanted)
        assertEquals(
            listOf("slug-a" to "foil", "slug-a" to "standard", "slug-b" to "standard"),
            rows.map { it.slug to it.finish },
        )
    }
}
