package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.MatchComputations
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.TradePayload
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchComputationsTest {

    private fun payload(
        pseudo: String? = null,
        tradeable: List<PayloadEntry> = emptyList(),
        wanted: List<PayloadEntry> = emptyList(),
    ) = TradePayload(pseudo = pseudo, tradeable = tradeable, wanted = wanted)

    @Test
    fun `je donne ce que je propose et que le pair recherche`() {
        val mine = payload(tradeable = listOf(PayloadEntry("slug-a", "standard", 3)))
        val peer = payload(wanted = listOf(PayloadEntry("slug-a", "standard", 1)))
        val match = MatchComputations.compute(mine, peer)
        assertEquals(listOf(MatchLine("slug-a", "standard", 1)), match.iCanGive)
        assertTrue(match.iCanReceive.isEmpty())
    }

    @Test
    fun `je recois ce que le pair propose et que je recherche`() {
        val mine = payload(wanted = listOf(PayloadEntry("slug-b", "foil", 2)))
        val peer = payload(tradeable = listOf(PayloadEntry("slug-b", "foil", 5)))
        val match = MatchComputations.compute(mine, peer)
        assertEquals(listOf(MatchLine("slug-b", "foil", 2)), match.iCanReceive)
        assertTrue(match.iCanGive.isEmpty())
    }

    @Test
    fun `quantite retenue est le minimum des deux cotes`() {
        val mine = payload(tradeable = listOf(PayloadEntry("slug-a", "standard", 2)))
        val peer = payload(wanted = listOf(PayloadEntry("slug-a", "standard", 5)))
        val match = MatchComputations.compute(mine, peer)
        assertEquals(2, match.iCanGive.single().quantity)
    }

    @Test
    fun `finish different ne matche pas`() {
        val mine = payload(tradeable = listOf(PayloadEntry("slug-a", "standard", 1)))
        val peer = payload(wanted = listOf(PayloadEntry("slug-a", "foil", 1)))
        val match = MatchComputations.compute(mine, peer)
        assertTrue(match.iCanGive.isEmpty())
        assertTrue(match.iCanReceive.isEmpty())
        assertTrue(!match.hasAnyMatch)
    }

    @Test
    fun `quantite nulle exclut la ligne`() {
        val mine = payload(tradeable = listOf(PayloadEntry("slug-a", "standard", 0)))
        val peer = payload(wanted = listOf(PayloadEntry("slug-a", "standard", 3)))
        assertTrue(MatchComputations.compute(mine, peer).iCanGive.isEmpty())
    }

    @Test
    fun `pseudo du pair est propage`() {
        val match = MatchComputations.compute(payload(pseudo = "Alice"), payload(pseudo = "Bob"))
        assertEquals("Bob", match.peerPseudo)
    }

    @Test
    fun `match est symetrique entre les deux points de vue`() {
        val alice = payload(
            pseudo = "Alice",
            tradeable = listOf(PayloadEntry("slug-a", "standard", 2)),
            wanted = listOf(PayloadEntry("slug-b", "foil", 1)),
        )
        val bob = payload(
            pseudo = "Bob",
            tradeable = listOf(PayloadEntry("slug-b", "foil", 4)),
            wanted = listOf(PayloadEntry("slug-a", "standard", 1)),
        )
        val aliceView = MatchComputations.compute(mine = alice, peer = bob)
        val bobView = MatchComputations.compute(mine = bob, peer = alice)
        // Ce qu'Alice donne = ce que Bob reçoit, et inversement.
        assertEquals(aliceView.iCanGive, bobView.iCanReceive)
        assertEquals(aliceView.iCanReceive, bobView.iCanGive)
    }

    @Test
    fun `resultats tries par slug puis finish`() {
        val mine = payload(
            tradeable = listOf(
                PayloadEntry("slug-b", "standard", 1),
                PayloadEntry("slug-a", "standard", 1),
                PayloadEntry("slug-a", "foil", 1),
            ),
        )
        val peer = payload(
            wanted = listOf(
                PayloadEntry("slug-a", "foil", 1),
                PayloadEntry("slug-a", "standard", 1),
                PayloadEntry("slug-b", "standard", 1),
            ),
        )
        val give = MatchComputations.compute(mine, peer).iCanGive
        assertEquals(
            listOf("slug-a" to "foil", "slug-a" to "standard", "slug-b" to "standard"),
            give.map { it.slug to it.finish },
        )
    }
}
