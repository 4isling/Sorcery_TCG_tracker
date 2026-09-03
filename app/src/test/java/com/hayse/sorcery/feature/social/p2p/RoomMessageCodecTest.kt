package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.RoomMessageCodec
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomMessageCodecTest {

    private fun roundTrip(message: RoomMessage): RoomMessage? =
        RoomMessageCodec.decode(RoomMessageCodec.encode(message))

    @Test
    fun `hello fait un aller-retour complet`() {
        val message = RoomMessage.Hello(pseudo = "Alice", schemaVersion = SCHEMA_VERSION)
        assertEquals(message, roundTrip(message))
    }

    @Test
    fun `chat fait un aller-retour complet`() {
        val message = RoomMessage.Chat(text = "Salut", sentAt = 1234L)
        assertEquals(message, roundTrip(message))
    }

    @Test
    fun `collection snapshot fait un aller-retour complet`() {
        val message = RoomMessage.CollectionSnapshot(
            entries = listOf(
                PayloadEntry("slug-a", "standard", 3),
                PayloadEntry("slug-b", "foil", 1),
            ),
        )
        assertEquals(message, roundTrip(message))
    }

    @Test
    fun `trade lists fait un aller-retour complet`() {
        val message = RoomMessage.TradeLists(
            tradeable = listOf(PayloadEntry("slug-a", "standard", 2)),
            wanted = listOf(PayloadEntry("slug-b", "foil", 1)),
        )
        assertEquals(message, roundTrip(message))
    }

    @Test
    fun `trade offer fait un aller-retour complet`() {
        val message = RoomMessage.TradeOffer(
            offerId = "offer-1",
            iGive = listOf(PayloadEntry("slug-a", "standard", 1)),
            iReceive = listOf(PayloadEntry("slug-b", "foil", 2)),
        )
        assertEquals(message, roundTrip(message))
    }

    @Test
    fun `trade response fait un aller-retour complet`() {
        val message = RoomMessage.TradeResponse(offerId = "offer-1", accepted = true)
        assertEquals(message, roundTrip(message))
    }

    @Test
    fun `hello avec version superieure reste decodable pour verification au handshake`() {
        // Le codec ne rejette PAS les versions : la compatibilité est vérifiée par le ViewModel
        // à partir de Hello.schemaVersion. Le champ doit donc être préservé tel quel.
        val raw = """{"type":"hello","pseudo":"Bob","schemaVersion":${SCHEMA_VERSION + 1}}"""
        val decoded = RoomMessageCodec.decode(raw)
        assertTrue(decoded is RoomMessage.Hello)
        assertEquals(SCHEMA_VERSION + 1, (decoded as RoomMessage.Hello).schemaVersion)
    }

    @Test
    fun `champs inconnus sont ignores`() {
        val raw = """{"type":"chat","text":"Coucou","sentAt":42,"futureField":true}"""
        val decoded = RoomMessageCodec.decode(raw)
        assertEquals(RoomMessage.Chat(text = "Coucou", sentAt = 42L), decoded)
    }

    @Test
    fun `type inconnu renvoie null`() {
        val raw = """{"type":"mysteryType","foo":"bar"}"""
        assertNull(RoomMessageCodec.decode(raw))
    }

    @Test
    fun `json invalide renvoie null`() {
        assertNull(RoomMessageCodec.decode("{ pas du json"))
    }
}
