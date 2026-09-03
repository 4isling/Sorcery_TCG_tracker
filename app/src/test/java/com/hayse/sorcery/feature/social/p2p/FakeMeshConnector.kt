package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.domain.p2p.MeshConnector
import com.hayse.sorcery.feature.social.domain.p2p.MeshEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * [MeshConnector] in-memory : substitut de Nearby `P2P_CLUSTER` pour les tests JVM. Plusieurs fakes
 * partagent un même [Bus] ; chacun adopte son `endpointId` = `localId` reçu à [start] (le fake
 * confond volontairement les deux, ce qui suffit au routage testé).
 *
 * `broadcast` diffuse un [MeshEvent.Frame] à tous les autres nœuds démarrés ; `sendTo` à un seul ;
 * l'arrivée/départ d'un nœud émet [MeshEvent.PeerJoined]/[MeshEvent.PeerLost] aux pairs. Le
 * `SharedFlow` a un `replay` large pour que la session, qui s'abonne après le premier événement,
 * ne perde aucune trame (comme `FakeLocalTransport`).
 */
class FakeMeshConnector(private val bus: Bus) : MeshConnector {

    private val _events = MutableSharedFlow<MeshEvent>(replay = 64, extraBufferCapacity = 64)
    override val events: Flow<MeshEvent> = _events.asSharedFlow()

    var endpointId: String = ""
        private set

    override suspend fun start(localId: String, localPseudo: String) {
        endpointId = localId
        bus.join(this)
    }

    override suspend fun broadcast(frame: String) = bus.broadcast(this, frame)

    override suspend fun sendTo(endpointId: String, frame: String) = bus.sendTo(this, endpointId, frame)

    override suspend fun stop() = bus.leave(this)

    private fun emit(event: MeshEvent) {
        _events.tryEmit(event)
    }

    /** Réseau partagé reliant plusieurs [FakeMeshConnector]. */
    class Bus {
        private val nodes = mutableListOf<FakeMeshConnector>()

        fun join(node: FakeMeshConnector) {
            nodes.forEach { other ->
                other.emit(MeshEvent.PeerJoined(node.endpointId))
                node.emit(MeshEvent.PeerJoined(other.endpointId))
            }
            nodes += node
        }

        fun leave(node: FakeMeshConnector) {
            if (!nodes.remove(node)) return
            nodes.forEach { it.emit(MeshEvent.PeerLost(node.endpointId)) }
        }

        fun broadcast(from: FakeMeshConnector, frame: String) {
            nodes.filter { it !== from }.forEach { it.emit(MeshEvent.Frame(from.endpointId, frame)) }
        }

        fun sendTo(from: FakeMeshConnector, endpointId: String, frame: String) {
            nodes.firstOrNull { it.endpointId == endpointId }
                ?.emit(MeshEvent.Frame(from.endpointId, frame))
        }
    }
}
