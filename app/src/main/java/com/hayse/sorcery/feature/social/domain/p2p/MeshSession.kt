package com.hayse.sorcery.feature.social.domain.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.MeshEnvelope
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import kotlinx.coroutines.flow.Flow

/**
 * Session du salon « Global » : couche au-dessus d'un [MeshConnector] qui (dé)sérialise les
 * [MeshEnvelope], tient le roster des participants et résout le routage privé.
 *
 * Analogue multi-pairs de [RoomSession] : là où RoomSession expose un unique pair, MeshSession
 * expose un [events] Flow enrichi (roster + messages identifiés) et deux modes d'envoi — diffusion
 * ([broadcast]) et privé adressé ([sendTo]).
 */
interface MeshSession {

    /** Flux d'événements décodés : évolutions du roster et messages reçus (avec identité). */
    val events: Flow<MeshSessionEvent>

    /** Diffuse un message à tout le cluster (`recipientId = null`). */
    suspend fun broadcast(body: RoomMessage)

    /** Envoie un message privé à un pair identifié par son `senderId` (deviceId stable). */
    suspend fun sendTo(recipientId: String, body: RoomMessage)

    /** Snapshot courant des participants connus. */
    fun roster(): List<MeshParticipant>

    /** Ferme la session et coupe le transport. */
    suspend fun close()
}

/** Un participant du salon, identifié par son [id] stable (deviceId) et son [endpointId] Nearby. */
data class MeshParticipant(
    val id: String,
    val pseudo: String,
    val endpointId: String,
)

/** Événement de haut niveau émis par une [MeshSession]. */
sealed interface MeshSessionEvent {
    /** Le roster a changé (arrivée, départ, ou pseudo découvert). Porte la liste à jour. */
    data class RosterChanged(val participants: List<MeshParticipant>) : MeshSessionEvent

    /** Message applicatif reçu d'un pair (diffusé ou privé), avec son enveloppe complète. */
    data class Message(val fromEndpointId: String, val envelope: MeshEnvelope) : MeshSessionEvent
}
