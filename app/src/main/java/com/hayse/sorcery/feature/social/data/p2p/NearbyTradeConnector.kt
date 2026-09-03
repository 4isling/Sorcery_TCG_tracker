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
import com.hayse.sorcery.feature.social.domain.p2p.LocalTransport
import com.hayse.sorcery.feature.social.domain.p2p.PairingRole
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken
import com.hayse.sorcery.feature.social.domain.p2p.TradeConnector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [TradeConnector] appuyé sur Nearby Connections en `P2P_POINT_TO_POINT` (mode A).
 *
 * L'hôte diffuse la session sous le nom d'endpoint `token.sessionId` ; l'invité ne se connecte
 * qu'à l'endpoint portant ce nom, garantissant l'appairage du bon appareil. Les trames reçues
 * sont republiées dans le [MutableSharedFlow] porté par le [NearbyLocalTransport] retourné.
 */
class NearbyTradeConnector(
    context: Context,
) : TradeConnector {

    private val client: ConnectionsClient = Nearby.getConnectionsClient(context.applicationContext)
    private val serviceId: String = "${context.applicationContext.packageName}.trade"
    private val strategy: Strategy = Strategy.P2P_POINT_TO_POINT

    override suspend fun connect(role: PairingRole, token: SessionToken): LocalTransport =
        suspendCancellableCoroutine { continuation ->
            // replay large : à l'ouverture d'une room, le pair envoie en rafale Hello +
            // CollectionSnapshot + TradeLists AVANT que le collecteur ne s'abonne (le VM fait
            // d'abord prepareLocal + ses propres envois). Sans abonné, un SharedFlow ne retient que
            // `replay` trames — avec replay=1 seule la dernière survivait, faisant perdre Hello et
            // la collection du pair. L'unique collecteur ne s'abonne qu'une fois : un replay large
            // n'a pas d'effet de bord et couvre le handshake initial.
            val received = MutableSharedFlow<String>(replay = 32, extraBufferCapacity = 64)
            val resumed = AtomicBoolean(false)

            fun fail(t: Throwable) {
                if (resumed.compareAndSet(false, true)) {
                    stopAll()
                    continuation.resumeWithException(t)
                }
            }

            fun succeed(endpointId: String) {
                if (resumed.compareAndSet(false, true)) {
                    client.stopAdvertising()
                    client.stopDiscovery()
                    continuation.resume(NearbyLocalTransport(client, endpointId, received))
                }
            }

            val payloadCallback = object : PayloadCallback() {
                override fun onPayloadReceived(endpointId: String, payload: Payload) {
                    payload.asBytes()?.let { received.tryEmit(String(it, Charsets.UTF_8)) }
                }

                override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
            }

            val lifecycleCallback = object : ConnectionLifecycleCallback() {
                override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                    client.acceptConnection(endpointId, payloadCallback)
                }

                override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
                    when (resolution.status.statusCode) {
                        ConnectionsStatusCodes.STATUS_OK -> succeed(endpointId)
                        else -> fail(IllegalStateException("connection failed: ${resolution.status.statusCode}"))
                    }
                }

                override fun onDisconnected(endpointId: String) = Unit
            }

            when (role) {
                PairingRole.HOST -> client.startAdvertising(
                    token.sessionId,
                    serviceId,
                    lifecycleCallback,
                    AdvertisingOptions.Builder().setStrategy(strategy).build(),
                ).addOnFailureListener { fail(it) }

                PairingRole.GUEST -> client.startDiscovery(
                    serviceId,
                    discoveryCallback(token, lifecycleCallback, ::fail),
                    DiscoveryOptions.Builder().setStrategy(strategy).build(),
                ).addOnFailureListener { fail(it) }
            }

            continuation.invokeOnCancellation { stopAll() }
        }

    private fun discoveryCallback(
        token: SessionToken,
        lifecycleCallback: ConnectionLifecycleCallback,
        onFailure: (Throwable) -> Unit,
    ) = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            if (info.endpointName == token.sessionId) {
                client.requestConnection(token.sessionId, endpointId, lifecycleCallback)
                    .addOnFailureListener { onFailure(it) }
            }
        }

        override fun onEndpointLost(endpointId: String) = Unit
    }

    private fun stopAll() {
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
    }
}
