package com.hayse.sorcery.feature.social.domain.p2p

import kotlinx.coroutines.flow.Flow

/**
 * Transport maillé « Global » : contrairement au [LocalTransport] 1:1, il gère N pairs simultanés
 * (Nearby `P2P_CLUSTER`). Chaque appareil advertise **et** discover en même temps et se connecte
 * automatiquement à tous les pairs à portée. Isolé du transport concret pour rester testable avec un
 * fake in-memory.
 *
 * Les trames sont des [String] JSON brutes ; la (dé)sérialisation (MeshEnvelope) est déléguée à la
 * couche session.
 */
interface MeshConnector {

    /** Événements bruts du maillage : arrivées/départs de pairs et trames reçues. */
    val events: Flow<MeshEvent>

    /**
     * Démarre l'advertise + discovery. [localId] est le `deviceId` stable (nom d'endpoint Nearby),
     * [localPseudo] est présenté aux pairs. Idempotent : stoppe toute session résiduelle d'abord.
     */
    suspend fun start(localId: String, localPseudo: String)

    /** Diffuse une trame à tous les pairs connectés. */
    suspend fun broadcast(frame: String)

    /** Envoie une trame à un pair précis (par son endpointId Nearby). */
    suspend fun sendTo(endpointId: String, frame: String)

    /** Coupe l'advertise/discovery et déconnecte tous les pairs. */
    suspend fun stop()
}

/** Événement bas niveau émis par un [MeshConnector]. */
sealed interface MeshEvent {
    /** Un pair vient de se connecter (identité pas encore connue tant que son Hello n'arrive pas). */
    data class PeerJoined(val endpointId: String) : MeshEvent

    /** Un pair s'est déconnecté ou est sorti de portée. */
    data class PeerLost(val endpointId: String) : MeshEvent

    /** Trame reçue d'un pair. */
    data class Frame(val fromEndpointId: String, val raw: String) : MeshEvent
}
