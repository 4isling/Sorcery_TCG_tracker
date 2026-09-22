package com.hayse.sorcery.di.modules

import com.hayse.sorcery.feature.cards.di.cardsModule
import com.hayse.sorcery.feature.collection.di.collectionModule
import com.hayse.sorcery.feature.deck.di.deckModule
import com.hayse.sorcery.feature.deckformat.di.deckFormatModule
import com.hayse.sorcery.feature.game_tracker.di.gameTrackerModule
import com.hayse.sorcery.feature.settings.di.settingsModule
import com.hayse.sorcery.feature.social.di.socialModule
import com.hayse.sorcery.feature.statistics.di.statisticsModule
import org.koin.core.module.Module

// Agrège les modules Koin par feature.
val featureModules: List<Module> = listOf(
    gameTrackerModule,
    cardsModule,
    collectionModule,
    deckModule,
    deckFormatModule,
    settingsModule,
    socialModule,
    statisticsModule,
)
