package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.domain.p2p.LocalTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Transport in-memory reliant deux extrémités : ce qu'une extrémité `send` apparaît dans
 * l'`incoming` de l'autre. Sert de substitut à Nearby Connections pour les tests JVM.
 */
class FakeLocalTransport private constructor(
    private val outbound: MutableSharedFlow<String>,
    override val incoming: Flow<String>,
) : LocalTransport {

    var closed: Boolean = false
        private set

    override suspend fun send(frame: String) {
        check(!closed) { "transport closed" }
        outbound.emit(frame)
    }

    override suspend fun close() {
        closed = true
    }

    companion object {
        /** Crée une paire liée (A ⇄ B). */
        fun linkedPair(): Pair<FakeLocalTransport, FakeLocalTransport> {
            val aToB = MutableSharedFlow<String>(replay = 16)
            val bToA = MutableSharedFlow<String>(replay = 16)
            val a = FakeLocalTransport(outbound = aToB, incoming = bToA)
            val b = FakeLocalTransport(outbound = bToA, incoming = aToB)
            return a to b
        }
    }
}
