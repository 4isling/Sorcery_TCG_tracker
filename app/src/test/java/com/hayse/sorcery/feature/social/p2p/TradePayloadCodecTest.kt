package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.DecodeResult
import com.hayse.sorcery.feature.social.data.p2p.TradePayloadCodec
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import com.hayse.sorcery.feature.social.data.p2p.model.TradePayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradePayloadCodecTest {

    @Test
    fun `encode puis decode conserve le payload`() {
        val payload = TradePayload(
            pseudo = "Alice",
            tradeable = listOf(PayloadEntry("slug-a", "standard", 2)),
            wanted = listOf(PayloadEntry("slug-b", "foil", 1)),
        )
        val result = TradePayloadCodec.decode(TradePayloadCodec.encode(payload))
        assertTrue(result is DecodeResult.Success)
        assertEquals(payload, (result as DecodeResult.Success).payload)
    }

    @Test
    fun `version plus recente est refusee`() {
        val raw = """{"schemaVersion":${SCHEMA_VERSION + 1},"tradeable":[],"wanted":[]}"""
        val result = TradePayloadCodec.decode(raw)
        assertTrue(result is DecodeResult.IncompatibleVersion)
        result as DecodeResult.IncompatibleVersion
        assertEquals(SCHEMA_VERSION + 1, result.theirs)
        assertEquals(SCHEMA_VERSION, result.ours)
    }

    @Test
    fun `champs inconnus sont ignores`() {
        val raw = """{"schemaVersion":$SCHEMA_VERSION,"pseudo":"Bob","futureField":42,"tradeable":[],"wanted":[]}"""
        val result = TradePayloadCodec.decode(raw)
        assertTrue(result is DecodeResult.Success)
        assertEquals("Bob", (result as DecodeResult.Success).payload.pseudo)
    }

    @Test
    fun `json invalide est signale malformed`() {
        val result = TradePayloadCodec.decode("{ not json")
        assertTrue(result is DecodeResult.Malformed)
    }

    @Test
    fun `payload minimal prend les valeurs par defaut`() {
        val result = TradePayloadCodec.decode("{}")
        assertTrue(result is DecodeResult.Success)
        val payload = (result as DecodeResult.Success).payload
        assertEquals(SCHEMA_VERSION, payload.schemaVersion)
        assertEquals(null, payload.pseudo)
        assertTrue(payload.tradeable.isEmpty())
        assertTrue(payload.wanted.isEmpty())
    }
}
