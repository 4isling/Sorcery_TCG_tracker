package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.MeshEnvelope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * (Dé)sérialise les [MeshEnvelope] du maillage « Global ». Logique pure, testable en JVM.
 *
 * Même configuration [Json] que `RoomMessageCodec` (`classDiscriminator = "type"`) : le [RoomMessage]
 * imbriqué dans [MeshEnvelope.body] conserve son discriminant polymorphe. Champs inconnus ignorés
 * (compat ascendante) ; trame illisible ou d'un type inconnu → `null` (ignorée par l'appelant).
 */
object MeshEnvelopeCodec {

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(envelope: MeshEnvelope): String =
        json.encodeToString(MeshEnvelope.serializer(), envelope)

    fun decode(raw: String): MeshEnvelope? = try {
        json.decodeFromString(MeshEnvelope.serializer(), raw)
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
