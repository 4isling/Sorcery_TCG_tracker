package com.hayse.sorcery.feature.game_tracker.data.local

import androidx.datastore.core.Serializer
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameHistoryData
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Serializer DataStore pour la liste des parties terminées. */
object GameHistorySerializer : Serializer<GameHistoryData> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: GameHistoryData = GameHistoryData()

    override suspend fun readFrom(input: InputStream): GameHistoryData {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            json.decodeFromString(GameHistoryData.serializer(), bytes.decodeToString())
        } catch (_: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: GameHistoryData, output: OutputStream) {
        output.write(json.encodeToString(GameHistoryData.serializer(), t).encodeToByteArray())
    }
}
