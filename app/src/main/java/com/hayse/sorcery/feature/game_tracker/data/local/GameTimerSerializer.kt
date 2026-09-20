package com.hayse.sorcery.feature.game_tracker.data.local

import androidx.datastore.core.Serializer
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameTimerData
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Serializer DataStore pour le chrono ; `null` = aucune partie chronométrée en cours. */
object GameTimerSerializer : Serializer<GameTimerData?> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: GameTimerData? = null

    override suspend fun readFrom(input: InputStream): GameTimerData? {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return null
        return try {
            json.decodeFromString(GameTimerData.serializer(), bytes.decodeToString())
        } catch (_: SerializationException) {
            null
        }
    }

    override suspend fun writeTo(t: GameTimerData?, output: OutputStream) {
        if (t == null) return
        output.write(json.encodeToString(GameTimerData.serializer(), t).encodeToByteArray())
    }
}
