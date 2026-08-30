package com.hayse.sorcery.feature.game_tracker.data.local

import androidx.datastore.core.Serializer
import com.hayse.sorcery.feature.game_tracker.data.local.model.PlayerPrefsData
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Serializer DataStore pour les préférences joueur. */
object PlayerPrefsSerializer : Serializer<PlayerPrefsData> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: PlayerPrefsData = PlayerPrefsData()

    override suspend fun readFrom(input: InputStream): PlayerPrefsData {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            json.decodeFromString(PlayerPrefsData.serializer(), bytes.decodeToString())
        } catch (_: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: PlayerPrefsData, output: OutputStream) {
        output.write(json.encodeToString(PlayerPrefsData.serializer(), t).encodeToByteArray())
    }
}
