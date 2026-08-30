package com.hayse.sorcery.feature.settings.data.repository

import androidx.datastore.core.DataStore
import com.hayse.sorcery.core.ui.theme.SorcerySet
import com.hayse.sorcery.feature.settings.data.local.model.SettingsData
import com.hayse.sorcery.feature.settings.domain.model.AppLanguage
import com.hayse.sorcery.feature.settings.domain.model.AppSettings
import com.hayse.sorcery.feature.settings.domain.model.ThemeMode
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    private fun SettingsData.toDomain(): AppSettings = AppSettings(
        themeMode = enumValues<ThemeMode>().firstOrNull { it.name == themeMode } ?: ThemeMode.SYSTEM,
        language = enumValues<AppLanguage>().firstOrNull { it.name == language } ?: AppLanguage.SYSTEM,
        themeSet = enumValues<SorcerySet>().firstOrNull { it.name == themeSet },
    )
}
