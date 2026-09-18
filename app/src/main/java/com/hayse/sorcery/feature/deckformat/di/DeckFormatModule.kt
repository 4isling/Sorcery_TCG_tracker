package com.hayse.sorcery.feature.deckformat.di

import com.hayse.sorcery.feature.deckformat.domain.usecase.GetPlayableCardsUseCase
import com.hayse.sorcery.feature.deckformat.domain.usecase.ValidateDeckUseCase
import org.koin.dsl.module

val deckFormatModule = module {
    factory { ValidateDeckUseCase(get()) }
    factory { GetPlayableCardsUseCase(get()) }
}
