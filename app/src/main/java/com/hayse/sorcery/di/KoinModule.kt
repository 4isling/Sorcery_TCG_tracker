package com.hayse.sorcery.di

import com.hayse.sorcery.di.modules.databaseModule
import com.hayse.sorcery.di.modules.dataStoreModule
import com.hayse.sorcery.di.modules.featureModules
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    databaseModule,
    dataStoreModule,
) + featureModules
