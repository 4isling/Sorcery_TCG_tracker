package com.hayse.sorcery.feature.social.domain.p2p

/**
 * Établit une [RoomSession] persistante à partir d'un rôle et d'un jeton de session. Réutilise le
 * bootstrap/handshake du transport 1:1 ; suspend jusqu'à ce que la connexion soit établie.
 */
interface RoomConnector {

    suspend fun connect(role: PairingRole, token: SessionToken): RoomSession
}
