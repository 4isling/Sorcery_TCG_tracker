package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.DecodeResult
import com.hayse.sorcery.feature.social.data.p2p.TradePayloadCodec
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.TradePayload
import com.hayse.sorcery.feature.social.domain.p2p.LocalTransport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Échange bout-en-bout mode A via le transport in-memory : encode → transport → decode → match. */
class TradeSessionExchangeTest {

    private val alice = TradePayload(
        pseudo = "Alice",
        tradeable = listOf(PayloadEntry("slug-a", "standard", 2)),
        wanted = listOf(PayloadEntry("slug-b", "foil", 1)),
    )
    private val bob = TradePayload(
        pseudo = "Bob",
        tradeable = listOf(PayloadEntry("slug-b", "foil", 4)),
        wanted = listOf(PayloadEntry("slug-a", "standard", 1)),
    )

    @Test
    fun `bootstrap round-trip token`() {
        val bootstrap = FakePairingBootstrap { "abc123" }
        val token = bootstrap.newToken()
        assertEquals(token, bootstrap.decodeToken(bootstrap.encodeToken(token)))
    }

    @Test
    fun `les payloads transitent sans perte par le transport`() = runBlocking {
        val (aTransport, bTransport) = FakeLocalTransport.linkedPair()

        // Chaque côté envoie son payload sérialisé ; l'autre le reçoit à l'identique.
        aTransport.send(TradePayloadCodec.encode(alice))
        bTransport.send(TradePayloadCodec.encode(bob))

        assertEquals(bob, receive(aTransport))
        assertEquals(alice, receive(bTransport))
    }

    @Test
    fun `envoi apres fermeture echoue`() = runBlocking {
        val (aTransport, _) = FakeLocalTransport.linkedPair()
        aTransport.close()
        assertTrue(aTransport.closed)
        val failed = runCatching { aTransport.send("x") }.isFailure
        assertTrue(failed)
    }

    private suspend fun receive(transport: LocalTransport): TradePayload {
        val result = TradePayloadCodec.decode(transport.incoming.first())
        assertTrue(result is DecodeResult.Success)
        return (result as DecodeResult.Success).payload
    }
}
