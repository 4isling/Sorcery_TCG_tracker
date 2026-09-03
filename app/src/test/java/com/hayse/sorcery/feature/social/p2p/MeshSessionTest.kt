package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.social.data.p2p.NearbyMeshSession
import com.hayse.sorcery.feature.social.data.p2p.model.MeshEnvelope
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.domain.p2p.MeshParticipant
import com.hayse.sorcery.feature.social.domain.p2p.MeshSession
import com.hayse.sorcery.feature.social.domain.p2p.MeshSessionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vérifie le routage de [NearbyMeshSession] au-dessus d'un [FakeMeshConnector] : roster construit
 * depuis les `Hello`, diffusion vue de tous, envoi privé livré au seul destinataire, destinataire
 * inconnu ignoré sans crash.
 */
class MeshSessionTest {

    private class Recorder {
        val rosters = mutableListOf<List<MeshParticipant>>()
        val messages = mutableListOf<MeshEnvelope>()

        fun on(event: MeshSessionEvent) = when (event) {
            is MeshSessionEvent.RosterChanged -> rosters += event.participants
            is MeshSessionEvent.Message -> messages += event.envelope
        }

        fun latestRoster(): List<MeshParticipant> = rosters.lastOrNull() ?: emptyList()
    }

    private fun CoroutineScope.record(session: MeshSession, recorder: Recorder): Job =
        launch { session.events.collect { recorder.on(it) } }

    private suspend fun waitUntil(timeoutMs: Long = 3000, predicate: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!predicate()) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw AssertionError("condition non atteinte dans le délai imparti")
            }
            delay(10)
        }
    }

    @Test
    fun `le roster se construit depuis les Hello des pairs`() = runBlocking {
        val bus = FakeMeshConnector.Bus()
        val sessionA = NearbyMeshSession(FakeMeshConnector(bus), "idA", "Alice")
        val sessionB = NearbyMeshSession(FakeMeshConnector(bus), "idB", "Bob")
        val recA = Recorder()
        val recB = Recorder()

        val jobA = record(sessionA, recA)
        val jobB = record(sessionB, recB)

        waitUntil {
            recA.latestRoster().any { it.id == "idB" } && recB.latestRoster().any { it.id == "idA" }
        }

        val bSeenByA = recA.latestRoster().first { it.id == "idB" }
        assertEquals("Bob", bSeenByA.pseudo)
        assertEquals("idB", bSeenByA.endpointId)
        val aSeenByB = recB.latestRoster().first { it.id == "idA" }
        assertEquals("Alice", aSeenByB.pseudo)

        jobA.cancel()
        jobB.cancel()
    }

    @Test
    fun `le depart d'un pair le retire du roster`() = runBlocking {
        val bus = FakeMeshConnector.Bus()
        val connB = FakeMeshConnector(bus)
        val sessionA = NearbyMeshSession(FakeMeshConnector(bus), "idA", "Alice")
        val sessionB = NearbyMeshSession(connB, "idB", "Bob")
        val recA = Recorder()

        val jobA = record(sessionA, recA)
        val jobB = record(sessionB, Recorder())

        waitUntil { recA.latestRoster().any { it.id == "idB" } }

        connB.stop()
        waitUntil { recA.latestRoster().none { it.id == "idB" } }
        assertTrue(recA.latestRoster().none { it.id == "idB" })

        jobA.cancel()
        jobB.cancel()
    }

    @Test
    fun `un chat diffuse est vu par tous les pairs`() = runBlocking {
        val bus = FakeMeshConnector.Bus()
        val sessionA = NearbyMeshSession(FakeMeshConnector(bus), "idA", "Alice")
        val sessionB = NearbyMeshSession(FakeMeshConnector(bus), "idB", "Bob")
        val recA = Recorder()
        val recB = Recorder()

        val jobA = record(sessionA, recA)
        val jobB = record(sessionB, recB)
        waitUntil { recA.latestRoster().any { it.id == "idB" } && recB.latestRoster().any { it.id == "idA" } }

        sessionA.broadcast(RoomMessage.Chat("bonjour le salon", 100L))

        waitUntil { recB.messages.any { it.body is RoomMessage.Chat } }
        val received = recB.messages.first { it.body is RoomMessage.Chat }
        assertEquals("idA", received.senderId)
        assertNull(received.recipientId)
        assertEquals(RoomMessage.Chat("bonjour le salon", 100L), received.body)

        jobA.cancel()
        jobB.cancel()
    }

    @Test
    fun `un envoi prive n'est livre qu'au destinataire`() = runBlocking {
        val bus = FakeMeshConnector.Bus()
        val sessionA = NearbyMeshSession(FakeMeshConnector(bus), "idA", "Alice")
        val sessionB = NearbyMeshSession(FakeMeshConnector(bus), "idB", "Bob")
        val sessionC = NearbyMeshSession(FakeMeshConnector(bus), "idC", "Carol")
        val recB = Recorder()
        val recC = Recorder()

        val jobA = record(sessionA, Recorder())
        val jobB = record(sessionB, recB)
        val jobC = record(sessionC, recC)

        // Attendre que A connaisse B (pour résoudre idB → endpointId).
        waitUntil { recB.latestRoster().any { it.id == "idA" } && recC.latestRoster().any { it.id == "idA" } }

        sessionA.sendTo("idB", RoomMessage.Chat("pour toi seule", 5L))

        waitUntil { recB.messages.any { it.body is RoomMessage.Chat } }
        val received = recB.messages.first { it.body is RoomMessage.Chat }
        assertEquals("idB", received.recipientId)
        assertEquals("idA", received.senderId)
        assertTrue(recC.messages.none { it.body is RoomMessage.Chat })

        jobA.cancel()
        jobB.cancel()
        jobC.cancel()
    }

    @Test
    fun `un destinataire inconnu est ignore sans crash`() = runBlocking {
        val bus = FakeMeshConnector.Bus()
        val sessionA = NearbyMeshSession(FakeMeshConnector(bus), "idA", "Alice")
        val jobA = record(sessionA, Recorder())

        // Aucun pair connu : sendTo doit être un no-op silencieux.
        sessionA.sendTo("fantome", RoomMessage.Chat("dans le vide", 1L))
        delay(50)

        jobA.cancel()
    }
}
