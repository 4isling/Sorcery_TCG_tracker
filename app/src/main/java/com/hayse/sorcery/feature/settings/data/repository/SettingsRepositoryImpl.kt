package com.hayse.sorcery.feature.settings.data.repository

import androidx.datastore.core.DataStore
import com.hayse.sorcery.core.ui.theme.SorcerySet
import com.hayse.sorcery.feature.settings.data.local.model.SettingsData
import com.hayse.sorcery.feature.settings.domain.model.AppLanguage
import com.hayse.sorcery.feature.settings.domain.model.AppSettings
import com.hayse.sorcery.feature.settings.domain.model.SkinMode
import com.hayse.sorcery.feature.settings.domain.model.ThemeMode
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SettingsRepositoryImpl(
    private val dataStore: DataStore<SettingsData>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { it.toDomain() }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.updateData { it.copy(themeMode = mode.name) }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.updateData { it.copy(language = language.name) }
    }

    override suspend fun setThemeSet(set: SorcerySet?) {
        dataStore.updateData { it.copy(themeSet = set?.name) }
    }

    override suspend fun setSkinMode(mode: SkinMode) {
        dataStore.updateData { it.copy(skinMode = mode.name) }
    }

    override suspend fun setSocialEnabled(enabled: Boolean) {
        dataStore.updateData { it.copy(socialEnabled = enabled) }
    }

    override suspend fun setPseudo(pseudo: String) {
        dataStore.updateData { it.copy(pseudo = pseudo) }
    }

    override suspend fun ensureDeviceId(): String =
        dataStore.updateData { current ->
            if (current.deviceId.isBlank()) current.copy(deviceId = UUID.randomUUID().toString()) else current
        }.deviceId

    private fun SettingsData.toDomain(): AppSettings = AppSettings(
        themeMode = enumValues<ThemeMode>().firstOrNull { it.name == themeMode } ?: ThemeMode.SYSTEM,
        language = enumValues<AppLanguage>().firstOrNull { it.name == language } ?: AppLanguage.SYSTEM,
        themeSet = enumValues<SorcerySet>().firstOrNull { it.name == themeSet },
        skinMode = enumValues<SkinMode>().firstOrNull { it.name == skinMode } ?: SkinMode.FIXED,
        socialEnabled = socialEnabled,
        pseudo = pseudo,
        deviceId = deviceId,
    )
}
