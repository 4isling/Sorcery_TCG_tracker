package com.hayse.sorcery.feature.settings.domain.repository

import com.hayse.sorcery.core.ui.theme.SorcerySet
import com.hayse.sorcery.feature.settings.domain.model.AppLanguage
import com.hayse.sorcery.feature.settings.domain.model.AppSettings
import com.hayse.sorcery.feature.settings.domain.model.SkinMode
import com.hayse.sorcery.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setThemeSet(set: SorcerySet?)
    suspend fun setSkinMode(mode: SkinMode)
    suspend fun setSocialEnabled(enabled: Boolean)
    suspend fun setPseudo(pseudo: String)

    /** Renvoie l'identité maillée de l'appareil, la générant (UUID) et la persistant si absente. */
    suspend fun ensureDeviceId(): String
}
