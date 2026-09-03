package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import com.hayse.sorcery.feature.social.data.p2p.model.TradePayload
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Résultat du décodage d'une trame reçue. */
sealed interface DecodeResult {
    data class Success(val payload: TradePayload) : DecodeResult

    /** Le pair parle une version plus récente que la nôtre : on ne peut pas garantir la lecture. */
    data class IncompatibleVersion(val theirs: Int, val ours: Int) : DecodeResult

    /** Trame illisible (JSON invalide ou champs manquants). */
    data class Malformed(val reason: String) : DecodeResult
}

/**
 * (Dé)sérialise les [TradePayload] échangés. Logique pure, sans dépendance Android/réseau.
 *
 * Politique de version : on lit tout payload dont `schemaVersion <= SCHEMA_VERSION` (les champs
 * inconnus sont ignorés, les champs absents prennent leur défaut → compatibilité ascendante).
 * Un payload plus récent que le nôtre est refusé explicitement.
 */
object TradePayloadCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(payload: TradePayload): String =
        json.encodeToString(TradePayload.serializer(), payload)

    fun decode(raw: String): DecodeResult {
        val payload = try {
            json.decodeFromString(TradePayload.serializer(), raw)
        } catch (e: SerializationException) {
            return DecodeResult.Malformed(e.message ?: "serialization error")
        } catch (e: IllegalArgumentException) {
            return DecodeResult.Malformed(e.message ?: "invalid payload")
        }
        if (payload.schemaVersion > SCHEMA_VERSION) {
            return DecodeResult.IncompatibleVersion(theirs = payload.schemaVersion, ours = SCHEMA_VERSION)
        }
        return DecodeResult.Success(payload)
    }
}
