package com.hayse.sorcery.feature.social.data.p2p.nfc

/**
 * Construction/lecture des trames APDU (ISO 7816) pour l'appairage NFC via HCE.
 *
 * Le token de session (forme encodée `ST<v>-<code>`) est renvoyé par l'hôte en réponse à un
 * SELECT AID. Aucune donnée d'échange ne transite : seulement ce token court.
 *
 * Toute la logique est pure (ByteArray) pour être testable sans matériel NFC.
 */
object NfcApdu {

    /** AID propriétaire (préfixe F0). 5 octets. */
    val AID: ByteArray = byteArrayOf(0xF0.toByte(), 0x53, 0x54, 0x43, 0x41)

    private val SELECT_HEADER = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00)
    private val SW_OK = byteArrayOf(0x90.toByte(), 0x00)
    private val SW_UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00)

    /** Commande SELECT AID envoyée par le lecteur : `00 A4 04 00 Lc <aid> 00`. */
    fun selectAidCommand(aid: ByteArray = AID): ByteArray =
        SELECT_HEADER + byteArrayOf(aid.size.toByte()) + aid + byteArrayOf(0x00)

    /** Vrai si [command] est un SELECT ciblant [aid]. */
    fun isSelectApdu(command: ByteArray, aid: ByteArray = AID): Boolean {
        if (command.size < 5) return false
        if (!command.copyOfRange(0, 4).contentEquals(SELECT_HEADER)) return false
        val lc = command[4].toInt() and 0xFF
        if (command.size < 5 + lc) return false
        return command.copyOfRange(5, 5 + lc).contentEquals(aid)
    }

    /** Réponse de l'hôte : octets UTF-8 du token suivis de SW 90 00. */
    fun buildTokenResponse(token: String): ByteArray =
        token.toByteArray(Charsets.UTF_8) + SW_OK

    /** Réponse d'erreur (SELECT inconnu ou aucun token à servir). */
    fun unknownResponse(): ByteArray = SW_UNKNOWN.copyOf()

    /** Extrait le token d'une réponse `<body> 90 00`, ou `null` si SW ≠ 90 00 ou corps vide. */
    fun parseTokenResponse(response: ByteArray): String? {
        if (response.size <= 2) return null
        val sw = response.copyOfRange(response.size - 2, response.size)
        if (!sw.contentEquals(SW_OK)) return null
        return response.copyOfRange(0, response.size - 2).toString(Charsets.UTF_8)
    }
}
