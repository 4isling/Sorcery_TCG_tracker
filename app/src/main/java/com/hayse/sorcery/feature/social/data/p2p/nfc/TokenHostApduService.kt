package com.hayse.sorcery.feature.social.data.p2p.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

/**
 * Émule une carte NFC servant le token de session ([HceTokenHolder]) en réponse à un SELECT AID.
 * Ne transporte jamais les listes d'échange — seulement le token, comme le QR (étape 4b).
 */
class TokenHostApduService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        val command = commandApdu ?: return NfcApdu.unknownResponse()
        if (!NfcApdu.isSelectApdu(command)) return NfcApdu.unknownResponse()
        val token = HceTokenHolder.token ?: return NfcApdu.unknownResponse()
        return NfcApdu.buildTokenResponse(token)
    }

    override fun onDeactivated(reason: Int) = Unit
}
