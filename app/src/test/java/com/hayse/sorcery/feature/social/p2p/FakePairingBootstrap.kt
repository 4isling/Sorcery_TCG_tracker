package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import com.hayse.sorcery.feature.social.domain.p2p.PairingBootstrap
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken

/**
 * Bootstrap déterministe pour les tests : encode le token en `sessionId:schemaVersion`.
 * [nextId] permet de fixer l'identifiant généré.
 */
class FakePairingBootstrap(
    private val nextId: () -> String = { "session-fake" },
) : PairingBootstrap {

    override fun newToken(): SessionToken = SessionToken(nextId(), SCHEMA_VERSION)

    override fun encodeToken(token: SessionToken): String = "${token.sessionId}:${token.schemaVersion}"

    override fun decodeToken(raw: String): SessionToken? {
        val idx = raw.lastIndexOf(':')
        if (idx <= 0 || idx == raw.length - 1) return null
        val version = raw.substring(idx + 1).toIntOrNull() ?: return null
        return SessionToken(raw.substring(0, idx), version)
    }
}
