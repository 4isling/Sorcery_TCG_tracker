package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.MeshEnvelope
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.domain.p2p.MeshConnector
import com.hayse.sorcery.feature.social.domain.p2p.MeshEvent
import com.hayse.sorcery.feature.social.domain.p2p.MeshParticipant
import com.hayse.sorcery.feature.social.domain.p2p.MeshSession
import com.hayse.sorcery.feature.social.domain.p2p.MeshSessionEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * [MeshSession] au-dessus d'un [MeshConnector]. Tient la **map bidirectionnelle
 * `endpointId ↔ senderId`** (clé du routage) alimentée par chaque enveloppe reçue : le `senderId`
 * stable est la source de vérité, l'`endpointId` Nearby n'étant qu'un handle éphémère.
 *
 * Au démarrage (première collecte de [events]) : s'abonne d'abord au connecteur, PUIS démarre
 * l'advertise/discovery et diffuse son `Hello` (ordre garantissant qu'aucune trame n'est perdue).
 * À chaque pair qui rejoint, renvoie un `Hello` ciblé pour un échange d'identités mutuel.
 */
class NearbyMeshSession(
    private val connector: MeshConnector,
    private val localId: String,
    private val localPseudo: String,
) : MeshSession {

    /** endpointId → participant connu (dès qu'une enveloppe de ce pair a été décodée). */
    private val byEndpoint = ConcurrentHashMap<String, MeshParticipant>()
    /** senderId stable → endpointId courant (pour résoudre le routage privé). */
    private val idToEndpoint = ConcurrentHashMap<String, String>()

    override val events: Flow<MeshSessionEvent> = channelFlow {
        val job = launch {
            connector.events.collect { event ->
                when (event) {
                    is MeshEvent.PeerJoined ->
                        // Identité inconnue tant que son Hello n'arrive pas ; on lui présente la nôtre.
                        connector.sendTo(event.endpointId, encodeHello())

                    is MeshEvent.PeerLost -> {
                        val removed = byEndpoint.remove(event.endpointId)
                        if (removed != null) {
                            idToEndpoint.remove(removed.id)
                            send(MeshSessionEvent.RosterChanged(roster()))
                        }
                    }

                    is MeshEvent.Frame -> {
                        val envelope = MeshEnvelopeCodec.decode(event.raw) ?: return@collect
                        val rosterChanged = upsertRoster(event.fromEndpointId, envelope)
                        if (rosterChanged) send(MeshSessionEvent.RosterChanged(roster()))
                        if (envelope.body !is RoomMessage.Hello) {
                            send(MeshSessionEvent.Message(event.fromEndpointId, envelope))
                        }
                    }
                }
            }
        }
        connector.start(localId, localPseudo)
        connector.broadcast(encodeHello())
        awaitClose { job.cancel() }
    }

    private fun upsertRoster(endpointId: String, envelope: MeshEnvelope): Boolean {
        val participant = MeshParticipant(envelope.senderId, envelope.senderPseudo, endpointId)
        val previous = byEndpoint.put(endpointId, participant)
        idToEndpoint[envelope.senderId] = endpointId
        return previous != participant
    }

    override suspend fun broadcast(body: RoomMessage) =
        connector.broadcast(encode(recipientId = null, body = body))

    override suspend fun sendTo(recipientId: String, body: RoomMessage) {
        val endpointId = idToEndpoint[recipientId] ?: return
        connector.sendTo(endpointId, encode(recipientId = recipientId, body = body))
    }

    override fun roster(): List<MeshParticipant> = byEndpoint.values.toList()

    override suspend fun close() = connector.stop()

    private fun encodeHello(): String = encode(recipientId = null, body = RoomMessage.Hello(localPseudo))

    private fun encode(recipientId: String?, body: RoomMessage): String =
        MeshEnvelopeCodec.encode(
            MeshEnvelope(
                senderId = localId,
                senderPseudo = localPseudo,
                recipientId = recipientId,
                body = body,
            ),
        )
}
