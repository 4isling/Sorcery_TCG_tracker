package com.hayse.sorcery.feature.social.domain.p2p

import kotlinx.coroutines.flow.Flow

/**
 * Canal de transport local pour l'échange P2P, isolé du reste du domaine : l'implémentation
 * réelle (Nearby Connections — étape 3) reste interchangeable avec un fake in-memory.
 *
 * Le transport manipule des trames JSON brutes ([String]) : la sérialisation est déléguée à
 * `TradePayloadCodec`, de sorte que le transport ignore le format des messages.
 */
interface LocalTransport {

    /** Trames reçues du pair. */
    val incoming: Flow<String>

    /** Envoie une trame au pair. */
    suspend fun send(frame: String)

    /** Ferme la session et libère les ressources. */
    suspend fun close()
}
