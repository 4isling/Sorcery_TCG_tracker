package com.hayse.sorcery.feature.game_tracker.data.local

import androidx.datastore.core.Serializer
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameStateData
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Serializer DataStore pour l'état de partie ; `null` = aucune partie en cours. */
object GameStateSerializer : Serializer<GameStateData?> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: GameStateData? = null

    override suspend fun readFrom(input: InputStream): GameStateData? {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return null
        return try {
            json.decodeFromString(GameStateData.serializer(), bytes.decodeToString())
        } catch (_: SerializationException) {
            null
        }
    }

    override suspend fun writeTo(t: GameStateData?, output: OutputStream) {
        if (t == null) return
        output.write(json.encodeToString(GameStateData.serializer(), t).encodeToByteArray())
    }
}
