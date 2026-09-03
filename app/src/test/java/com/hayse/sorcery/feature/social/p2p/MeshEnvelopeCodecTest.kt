package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.MeshEnvelopeCodec
import com.hayse.sorcery.feature.social.data.p2p.model.MeshEnvelope
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshEnvelopeCodecTest {

    private fun roundTrip(envelope: MeshEnvelope): MeshEnvelope? =
        MeshEnvelopeCodec.decode(MeshEnvelopeCodec.encode(envelope))

    private fun envelope(recipientId: String?, body: RoomMessage) = MeshEnvelope(
        senderId = "device-alice",
        senderPseudo = "Alice",
        recipientId = recipientId,
        body = body,
    )

    @Test
    fun `enveloppe de diffusion fait un aller-retour complet`() {
        val env = envelope(recipientId = null, body = RoomMessage.Chat("Salut le salon", 42L))
        assertEquals(env, roundTrip(env))
    }

    @Test
    fun `enveloppe privee preserve le destinataire`() {
        val env = envelope(recipientId = "device-bob", body = RoomMessage.Chat("En privé", 7L))
        val decoded = roundTrip(env)
        assertEquals(env, decoded)
        assertEquals("device-bob", decoded?.recipientId)
    }

    @Test
    fun `chaque variante de body imbriquee survit a l'enveloppe`() {
        val bodies = listOf(
            RoomMessage.Hello(pseudo = "Alice", schemaVersion = SCHEMA_VERSION),
            RoomMessage.Chat(text = "hey", sentAt = 1L),
            RoomMessage.CollectionSnapshot(entries = listOf(PayloadEntry("slug-a", "standard", 3))),
            RoomMessage.TradeLists(
                tradeable = listOf(PayloadEntry("slug-a", "standard", 2)),
                wanted = listOf(PayloadEntry("slug-b", "foil", 1)),
            ),
            RoomMessage.TradeOffer(
                offerId = "offer-1",
                iGive = listOf(PayloadEntry("slug-a", "standard", 1)),
                iReceive = listOf(PayloadEntry("slug-b", "foil", 2)),
            ),
            RoomMessage.TradeResponse(offerId = "offer-1", accepted = true),
        )
        bodies.forEach { body ->
            val env = envelope(recipientId = null, body = body)
            assertEquals(body, roundTrip(env)?.body)
        }
    }

    @Test
    fun `le discriminant du body imbrique est preserve`() {
        val env = envelope(recipientId = null, body = RoomMessage.TradeResponse("offer-1", accepted = false))
        val raw = MeshEnvelopeCodec.encode(env)
        assertTrue(raw.contains("\"type\":\"response\""))
    }

    @Test
    fun `champ inconnu au niveau enveloppe est ignore`() {
        val raw = """
            {"senderId":"device-alice","senderPseudo":"Alice","recipientId":null,
             "body":{"type":"chat","text":"Coucou","sentAt":9},"schemaVersion":$SCHEMA_VERSION,
             "futureField":true}
        """.trimIndent()
        val decoded = MeshEnvelopeCodec.decode(raw)
        assertEquals("device-alice", decoded?.senderId)
        assertEquals(RoomMessage.Chat("Coucou", 9L), decoded?.body)
    }

    @Test
    fun `trame malformee renvoie null`() {
        assertNull(MeshEnvelopeCodec.decode("{ pas du json"))
    }

    @Test
    fun `body de type inconnu renvoie null`() {
        val raw = """{"senderId":"a","senderPseudo":"A","body":{"type":"mystere"}}"""
        assertNull(MeshEnvelopeCodec.decode(raw))
    }
}
