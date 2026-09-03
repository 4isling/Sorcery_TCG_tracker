package com.hayse.sorcery.feature.social.data.p2p

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.hayse.sorcery.feature.social.domain.p2p.MeshConnector
import com.hayse.sorcery.feature.social.domain.p2p.MeshEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * [MeshConnector] au-dessus de Nearby Connections en `P2P_CLUSTER` (salon « Global »).
 *
 * Découverte OUVERTE : advertise + discovery simultanés sur le serviceId `<pkg>.global`, et
 * `acceptConnection` automatique pour tout endpoint (aucun code/token). Acceptable car les données
 * sont non sensibles (collections publiques, chat, propositions) et un échange ne modifie la
 * collection qu'après accept explicite dans l'UI. Le `senderId` reste auto-déclaré (pas d'auth, pas
 * de chiffrement) — limite connue, exposition bornée au premier plan par le cycle de vie de l'écran.
 *
 * serviceId + stratégie distincts de `NearbyTradeConnector` (`<pkg>.trade`/POINT_TO_POINT) : les
 * deux modes ne tournent jamais simultanément (un seul écran actif à la fois).
 */
class NearbyMeshConnector(
    context: Context,
) : MeshConnector {

    private val client: ConnectionsClient = Nearby.getConnectionsClient(context.applicationContext)
    private val serviceId: String = "${context.applicationContext.packageName}.global"
    private val strategy: Strategy = Strategy.P2P_CLUSTER

    private val _events = MutableSharedFlow<MeshEvent>(extraBufferCapacity = 64)
    override val events: Flow<MeshEvent> = _events.asSharedFlow()

    /** endpoints vivants (connectés). Concurrent : callbacks Nearby sur threads GMS. */
    private val liveEndpoints = ConcurrentHashMap.newKeySet<String>()
    private var localId: String = ""

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { _events.tryEmit(MeshEvent.Frame(endpointId, String(it, Charsets.UTF_8))) }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                // Advertise+discover ⇒ une tentative de chaque côté ; ne signaler qu'une arrivée.
                if (liveEndpoints.add(endpointId)) _events.tryEmit(MeshEvent.PeerJoined(endpointId))
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (liveEndpoints.remove(endpointId)) _events.tryEmit(MeshEvent.PeerLost(endpointId))
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            client.requestConnection(localId, endpointId, lifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            if (liveEndpoints.remove(endpointId)) _events.tryEmit(MeshEvent.PeerLost(endpointId))
        }
    }

    override suspend fun start(localId: String, localPseudo: String) {
        this.localId = localId
        // Défensif : repartir d'un état propre si une session précédente n'a pas été coupée.
        client.stopAdvertising()
        client.stopDiscovery()
        client.startAdvertising(
            localId,
            serviceId,
            lifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(strategy).build(),
        )
        client.startDiscovery(
            serviceId,
            discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(strategy).build(),
        )
    }

    override suspend fun broadcast(frame: String) {
        val payload = Payload.fromBytes(frame.toByteArray(Charsets.UTF_8))
        liveEndpoints.forEach { client.sendPayload(it, payload) }
    }

    override suspend fun sendTo(endpointId: String, frame: String) {
        if (endpointId in liveEndpoints) {
            client.sendPayload(endpointId, Payload.fromBytes(frame.toByteArray(Charsets.UTF_8)))
        }
    }

    override suspend fun stop() {
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
        liveEndpoints.clear()
    }
}
