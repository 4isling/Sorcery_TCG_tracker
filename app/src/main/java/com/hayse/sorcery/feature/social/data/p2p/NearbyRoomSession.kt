package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.domain.p2p.LocalTransport
import com.hayse.sorcery.feature.social.domain.p2p.RoomSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * [RoomSession] adossée à un [LocalTransport] (déjà bidirectionnel) : les [RoomMessage] sont
 * (dé)sérialisés via [RoomMessageCodec] et les trames illisibles/de type inconnu sont ignorées.
 */
class NearbyRoomSession(
    private val transport: LocalTransport,
) : RoomSession {

    override val incoming: Flow<RoomMessage> =
        transport.incoming.mapNotNull { RoomMessageCodec.decode(it) }

    override suspend fun send(message: RoomMessage) {
        transport.send(RoomMessageCodec.encode(message))
    }

    override suspend fun close() {
        transport.close()
    }
}
