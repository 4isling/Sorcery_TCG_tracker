package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.domain.p2p.PairingRole
import com.hayse.sorcery.feature.social.domain.p2p.RoomConnector
import com.hayse.sorcery.feature.social.domain.p2p.RoomSession
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken
import com.hayse.sorcery.feature.social.domain.p2p.TradeConnector

/**
 * [RoomConnector] qui réutilise le [TradeConnector] existant (Nearby P2P_POINT_TO_POINT) pour ouvrir
 * le [LocalTransport], puis l'enveloppe dans une [NearbyRoomSession] persistante multi-messages.
 */
class NearbyRoomConnector(
    private val connector: TradeConnector,
) : RoomConnector {

    override suspend fun connect(role: PairingRole, token: SessionToken): RoomSession =
        NearbyRoomSession(connector.connect(role, token))
}
