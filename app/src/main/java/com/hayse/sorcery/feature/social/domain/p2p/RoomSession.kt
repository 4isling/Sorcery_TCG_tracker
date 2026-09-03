package com.hayse.sorcery.feature.social.domain.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import kotlinx.coroutines.flow.Flow

/**
 * Session de room interactive : canal bidirectionnel persistant sur lequel les deux appareils
 * échangent des [RoomMessage] tant que la room est ouverte (chat, collection, offres). Isolé du
 * transport concret (Nearby) pour rester testable avec un fake.
 */
interface RoomSession {

    /** Messages décodés reçus du pair. */
    val incoming: Flow<RoomMessage>

    /** Envoie un message au pair. */
    suspend fun send(message: RoomMessage)

    /** Ferme la session et libère les ressources. */
    suspend fun close()
}
