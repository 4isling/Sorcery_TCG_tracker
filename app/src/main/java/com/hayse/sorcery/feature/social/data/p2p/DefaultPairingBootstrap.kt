package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import com.hayse.sorcery.feature.social.domain.p2p.PairingBootstrap
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken
import kotlin.random.Random

/**
 * Implémentation par défaut du bootstrap : le [SessionToken] est un code court, saisissable à la
 * main (étape 4a) et encodable tel quel dans un QR ou une trame NFC (étapes 4b/4c).
 *
 * Forme encodée : `ST<version>-<code>` (ex. `ST1-K7Q9ZР`). Le code utilise un alphabet sans
 * caractères ambigus (pas de 0/O, 1/I) pour la lecture humaine.
 */
class DefaultPairingBootstrap(
    private val random: Random = Random.Default,
) : PairingBootstrap {

    override fun newToken(): SessionToken =
        SessionToken(sessionId = randomCode(), schemaVersion = SCHEMA_VERSION)

    override fun encodeToken(token: SessionToken): String =
        "$PREFIX${token.schemaVersion}$SEPARATOR${token.sessionId}"

    override fun decodeToken(raw: String): SessionToken? {
        val trimmed = raw.trim()
        if (!trimmed.startsWith(PREFIX)) return null
        val sepIndex = trimmed.indexOf(SEPARATOR, startIndex = PREFIX.length)
        if (sepIndex <= PREFIX.length) return null
        val version = trimmed.substring(PREFIX.length, sepIndex).toIntOrNull() ?: return null
        val code = trimmed.substring(sepIndex + 1)
        if (code.isEmpty() || code.any { it !in ALPHABET }) return null
        return SessionToken(code, version)
    }

    private fun randomCode(): String =
        buildString { repeat(CODE_LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }

    private companion object {
        const val PREFIX = "ST"
        const val SEPARATOR = '-'
        const val CODE_LENGTH = 6
        const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
