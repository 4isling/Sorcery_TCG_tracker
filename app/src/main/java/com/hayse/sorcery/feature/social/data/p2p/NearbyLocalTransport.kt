package com.hayse.sorcery.feature.social.data.p2p

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.hayse.sorcery.feature.social.domain.p2p.LocalTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * [LocalTransport] au-dessus d'un endpoint Nearby Connections déjà établi. Les trames sont
 * transmises en `Payload.Type.BYTES` (UTF-8) ; la (dé)sérialisation reste hors du transport.
 */
class NearbyLocalTransport(
    private val connectionsClient: ConnectionsClient,
    private val endpointId: String,
    private val received: MutableSharedFlow<String>,
) : LocalTransport {

    override val incoming: Flow<String> = received.asSharedFlow()

    override suspend fun send(frame: String) {
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(frame.toByteArray(Charsets.UTF_8)))
    }

    override suspend fun close() {
        connectionsClient.disconnectFromEndpoint(endpointId)
        connectionsClient.stopAllEndpoints()
    }
}
