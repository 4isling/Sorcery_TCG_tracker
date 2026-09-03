package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.domain.p2p.LocalTransport
import com.hayse.sorcery.feature.social.domain.p2p.PairingRole
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken
import com.hayse.sorcery.feature.social.domain.p2p.TradeConnector

/**
 * Connecteur de test : retourne le [transport] fourni, ou lève [failWith] si présent (simule un
 * échec de handshake). Enregistre le dernier rôle demandé.
 */
class FakeTradeConnector(
    private val transport: LocalTransport,
    private val failWith: Throwable? = null,
) : TradeConnector {

    var lastRole: PairingRole? = null
        private set

    override suspend fun connect(role: PairingRole, token: SessionToken): LocalTransport {
        lastRole = role
        failWith?.let { throw it }
        return transport
    }
}
