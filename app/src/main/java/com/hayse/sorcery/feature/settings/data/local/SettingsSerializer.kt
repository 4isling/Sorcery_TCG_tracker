package com.hayse.sorcery.feature.settings.data.local

import androidx.datastore.core.Serializer
import com.hayse.sorcery.feature.settings.data.local.model.SettingsData
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Serializer DataStore pour les préférences de l'app. */
object SettingsSerializer : Serializer<SettingsData> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: SettingsData = SettingsData()

    override suspend fun readFrom(input: InputStream): SettingsData {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            json.decodeFromString(SettingsData.serializer(), bytes.decodeToString())
        } catch (_: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SettingsData, output: OutputStream) {
        output.write(json.encodeToString(SettingsData.serializer(), t).encodeToByteArray())
    }
}
