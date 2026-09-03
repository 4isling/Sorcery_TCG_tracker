package com.hayse.sorcery.feature.social.domain.p2p

/**
 * Établit un [LocalTransport] connecté à partir d'un rôle et d'un jeton de session. Isole la
 * découverte/handshake du transport concret (Nearby Connections) du reste du domaine, ce qui rend
 * l'orchestration de session testable avec un connecteur fake.
 */
interface TradeConnector {

    /** Ouvre la session ; suspend jusqu'à ce que la connexion soit établie. */
    suspend fun connect(role: PairingRole, token: SessionToken): LocalTransport
}
