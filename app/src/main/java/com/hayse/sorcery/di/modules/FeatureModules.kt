package com.hayse.sorcery.di.modules

import com.hayse.sorcery.feature.cards.di.cardsModule
import com.hayse.sorcery.feature.collection.di.collectionModule
import com.hayse.sorcery.feature.game_tracker.di.gameTrackerModule
import org.koin.core.module.Module

// Agrège les modules Koin par feature.
val featureModules: List<Module> = listOf(
    gameTrackerModule,
    cardsModule,
    collectionModule,
)
