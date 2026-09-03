package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.DefaultPairingBootstrap
import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DefaultPairingBootstrapTest {

    private val bootstrap = DefaultPairingBootstrap(Random(42))

    @Test
    fun `newToken produit un code de la bonne longueur et alphabet`() {
        val token = bootstrap.newToken()
        assertEquals(SCHEMA_VERSION, token.schemaVersion)
        assertEquals(6, token.sessionId.length)
        assertTrue(token.sessionId.all { it in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" })
    }

    @Test
    fun `encode puis decode round-trip`() {
        val token = bootstrap.newToken()
        assertEquals(token, bootstrap.decodeToken(bootstrap.encodeToken(token)))
    }

    @Test
    fun `decode tolere les espaces autour`() {
        val token = SessionToken("K7Q9ZB", SCHEMA_VERSION)
        assertEquals(token, bootstrap.decodeToken("  ${bootstrap.encodeToken(token)}  "))
    }

    @Test
    fun `decode rejette un prefixe absent`() {
        assertNull(bootstrap.decodeToken("XX1-ABC"))
    }

    @Test
    fun `decode rejette une version non numerique`() {
        assertNull(bootstrap.decodeToken("STx-ABCDEF"))
    }

    @Test
    fun `decode rejette un code vide`() {
        assertNull(bootstrap.decodeToken("ST1-"))
    }

    @Test
    fun `decode rejette un caractere ambigu dans le code`() {
        assertNull(bootstrap.decodeToken("ST1-ABC0DE"))
    }
}
