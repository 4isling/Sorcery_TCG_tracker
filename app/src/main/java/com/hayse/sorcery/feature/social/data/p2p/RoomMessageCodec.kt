package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * (Dé)sérialise les [RoomMessage] d'une session interactive. Logique pure, sans dépendance
 * Android/réseau (testable en JVM).
 *
 * Le JSON est polymorphe : chaque trame porte un champ `type` (le [kotlinx.serialization.SerialName]
 * de la variante). Les champs inconnus sont ignorés (compatibilité ascendante) ; une trame illisible
 * ou d'un type inconnu renvoie `null` et est simplement ignorée par l'appelant. La compatibilité de
 * version est vérifiée séparément via [RoomMessage.Hello.schemaVersion] au handshake.
 */
object RoomMessageCodec {

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(message: RoomMessage): String =
        json.encodeToString(RoomMessage.serializer(), message)

    fun decode(raw: String): RoomMessage? = try {
        json.decodeFromString(RoomMessage.serializer(), raw)
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
