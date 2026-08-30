package com.hayse.sorcery.feature.settings.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.hayse.sorcery.feature.settings.data.local.SettingsSerializer
import com.hayse.sorcery.feature.settings.data.local.model.SettingsData
import com.hayse.sorcery.feature.settings.data.repository.SettingsRepositoryImpl
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import com.hayse.sorcery.feature.settings.ui.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val settingsStore = named("settings_store")

val settingsModule = module {
    single<DataStore<SettingsData>>(settingsStore) {
        DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = { androidContext().dataStoreFile("settings.json") },
        )
    }

    single<SettingsRepository> { SettingsRepositoryImpl(get(settingsStore)) }

    viewModel { SettingsViewModel(get()) }
}
