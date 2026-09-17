package com.hayse.sorcery.feature.social

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.social.data.p2p.TradeSuggestionComputations
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.domain.model.SuggestionReason
import com.hayse.sorcery.feature.social.domain.repository.SuggestionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TradeSuggestionComputationsTest {

    private data class CardDef(val name: String, val rarity: Rarity?, val printings: List<Pair<String, String>>)

    private fun catalog(vararg cards: CardDef): SuggestionCatalog {
        val printingToCard = HashMap<String, String>()
        val rarityByCard = HashMap<String, Rarity?>()
        val setCardsBySet = HashMap<String, MutableSet<String>>()
        val setsByCard = HashMap<String, MutableSet<String>>()
        val printingsByCard = HashMap<String, MutableList<String>>()
        for (c in cards) {
            rarityByCard[c.name] = c.rarity
            for ((slug, set) in c.printings) {
                printingToCard[slug] = c.name
                setCardsBySet.getOrPut(set) { mutableSetOf() }.add(c.name)
                setsByCard.getOrPut(c.name) { mutableSetOf() }.add(set)
                printingsByCard.getOrPut(c.name) { mutableListOf() }.add(slug)
            }
        }
        return SuggestionCatalog(
            printingToCard = printingToCard,
            rarityByCard = rarityByCard,
            setCardsBySet = setCardsBySet.mapValues { it.value.toSet() },
            setsByCard = setsByCard.mapValues { it.value.toSet() },
            printingsByCard = printingsByCard,
        )
    }

    private fun p(slug: String, qty: Int, finish: String = "Standard") = PayloadEntry(slug, finish, qty)

    private fun compute(
        catalog: SuggestionCatalog,
        myCollection: List<PayloadEntry> = emptyList(),
        peerCollection: List<PayloadEntry> = emptyList(),
        myTradeable: List<PayloadEntry> = emptyList(),
        myWanted: List<PayloadEntry> = emptyList(),
        peerTradeable: List<PayloadEntry> = emptyList(),
        peerWanted: List<PayloadEntry> = emptyList(),
    ) = TradeSuggestionComputations.compute(
        peerPseudo = "Bob",
        myCollection = myCollection,
        peerCollection = peerCollection,
        myTradeable = myTradeable,
        myWanted = myWanted,
        peerTradeable = peerTradeable,
        peerWanted = peerWanted,
        catalog = catalog,
    )

    @Test
    fun `surplus au-dela du playset comble une carte manquante du pair`() {
        val cat = catalog(CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")))
        val result = compute(cat, myCollection = listOf(p("a", 5)))

        val give = result.iCanGive.single()
        assertEquals("A", give.cardName)
        assertEquals("a", give.slug)
        assertEquals(1, give.quantity) // 5 possédés - 4 max = 1 surplus
        assertEquals(SuggestionReason.Missing, give.reason)
        assertTrue(result.iCanReceive.isEmpty())
    }

    @Test
    fun `pas de surplus au playset et sans liste a echanger`() {
        val cat = catalog(CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")))
        val result = compute(cat, myCollection = listOf(p("a", 4)))
        assertTrue(result.iCanGive.isEmpty())
    }

    @Test
    fun `une carte hors surplus mais marquee a echanger est proposable`() {
        val cat = catalog(CardDef("B", Rarity.Ordinary, listOf("b" to "Alpha")))
        val result = compute(
            cat,
            myCollection = listOf(p("b", 1)),
            myTradeable = listOf(p("b", 1)),
        )
        val give = result.iCanGive.single()
        assertEquals("B", give.cardName)
        assertEquals(SuggestionReason.Missing, give.reason)
    }

    @Test
    fun `priorite recherche puis set puis manquante`() {
        val cat = catalog(
            CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")), // recherché par le pair
            CardDef("B", Rarity.Ordinary, listOf("b" to "Alpha")), // complète un set commencé (Alpha)
            CardDef("C", Rarity.Ordinary, listOf("c" to "Gothic")), // simplement manquante
            CardDef("Seed", Rarity.Ordinary, listOf("seed" to "Alpha")), // possédée par le pair -> Alpha commencé
        )
        val result = compute(
            cat,
            myCollection = listOf(p("a", 5), p("b", 5), p("c", 5)),
            peerCollection = listOf(p("seed", 1)),
            peerWanted = listOf(p("a", 1)),
        )
        val reasons = result.iCanGive.associate { it.cardName to it.reason }
        assertEquals(SuggestionReason.Wanted, reasons["A"])
        assertEquals(SuggestionReason.CompletesSet, reasons["B"])
        assertEquals(SuggestionReason.Missing, reasons["C"])
        // Tri par priorité : Wanted, CompletesSet, Missing.
        assertEquals(listOf("A", "B", "C"), result.iCanGive.map { it.cardName })
    }

    @Test
    fun `granularite souple - un surplus foil comble un besoin de la carte`() {
        val cat = catalog(CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")))
        val result = compute(cat, myCollection = listOf(p("a", 5, finish = "Foil")))
        val give = result.iCanGive.single()
        assertEquals("A", give.cardName)
        assertEquals("Foil", give.finish)
    }

    @Test
    fun `aucune suggestion si le pair possede deja la carte et ne la recherche pas`() {
        val cat = catalog(CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")))
        val result = compute(
            cat,
            myCollection = listOf(p("a", 5)),
            peerCollection = listOf(p("a", 1)),
        )
        assertTrue(result.iCanGive.isEmpty())
    }

    @Test
    fun `symetrie - le surplus du pair devient une reception pour moi`() {
        val cat = catalog(CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")))
        val result = compute(cat, peerCollection = listOf(p("a", 5)))
        val receive = result.iCanReceive.single()
        assertEquals("A", receive.cardName)
        assertEquals(1, receive.quantity)
        assertEquals(SuggestionReason.Missing, receive.reason)
        assertTrue(result.iCanGive.isEmpty())
    }

    @Test
    fun `carte recherchee honore la quantite demandee`() {
        val cat = catalog(CardDef("A", Rarity.Ordinary, listOf("a" to "Alpha")))
        // 6 possédés -> surplus 2 ; le pair en recherche 2.
        val result = compute(
            cat,
            myCollection = listOf(p("a", 6)),
            peerWanted = listOf(p("a", 2)),
        )
        assertEquals(2, result.iCanGive.single().quantity)
    }

    @Test
    fun `rarete inconnue - pas de surplus calculable`() {
        val cat = catalog(CardDef("A", null, listOf("a" to "Alpha")))
        val result = compute(cat, myCollection = listOf(p("a", 5)))
        assertTrue(result.iCanGive.isEmpty())
        assertNull(cat.rarityByCard["A"])
    }
}
