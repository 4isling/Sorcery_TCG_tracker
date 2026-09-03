package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.nfc.NfcApdu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcApduTest {

    @Test
    fun `selectAidCommand est reconnu par isSelectApdu`() {
        val command = NfcApdu.selectAidCommand()
        assertTrue(NfcApdu.isSelectApdu(command))
    }

    @Test
    fun `isSelectApdu refuse un AID different`() {
        val command = NfcApdu.selectAidCommand(byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04))
        assertFalse(NfcApdu.isSelectApdu(command))
    }

    @Test
    fun `isSelectApdu refuse une trame trop courte`() {
        assertFalse(NfcApdu.isSelectApdu(byteArrayOf(0x00, 0xA4.toByte())))
    }

    @Test
    fun `round-trip token via reponse APDU`() {
        val token = "ST1-K7Q9ZP"
        val response = NfcApdu.buildTokenResponse(token)
        assertEquals(token, NfcApdu.parseTokenResponse(response))
    }

    @Test
    fun `parseTokenResponse rejette un statut non-OK`() {
        val bad = "ST1-K7Q9ZP".toByteArray(Charsets.UTF_8) + byteArrayOf(0x6F.toByte(), 0x00)
        assertNull(NfcApdu.parseTokenResponse(bad))
    }

    @Test
    fun `parseTokenResponse rejette un corps vide`() {
        assertNull(NfcApdu.parseTokenResponse(NfcApdu.unknownResponse()))
        assertNull(NfcApdu.parseTokenResponse(byteArrayOf(0x90.toByte(), 0x00)))
    }
}
