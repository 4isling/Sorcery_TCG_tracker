package com.hayse.sorcery.feature.cards.di

import com.hayse.sorcery.feature.cards.data.local.CardCatalogSeeder
import com.hayse.sorcery.feature.cards.data.repository.CardImageResolver
import com.hayse.sorcery.feature.cards.data.repository.CardRepositoryImpl
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.cards.domain.usecase.GetCardDetailUseCase
import com.hayse.sorcery.feature.cards.domain.usecase.ObserveCardsUseCase
import com.hayse.sorcery.feature.cards.ui.viewmodel.CardBrowserViewModel
import com.hayse.sorcery.feature.cards.ui.viewmodel.CardDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cardsModule = module {
    single { CardCatalogSeeder(androidContext(), get()) }
    single { CardImageResolver(androidContext()) }
    single<CardRepository> { CardRepositoryImpl(dao = get(), collectionDao = get(), seeder = get(), imageResolver = get()) }

    factory { ObserveCardsUseCase(get()) }
    factory { GetCardDetailUseCase(get()) }

    viewModel { CardBrowserViewModel(repository = get(), observeCards = get()) }
    viewModel { (name: String) ->
        CardDetailViewModel(
            name = name,
            getCardDetail = get(),
            observeOwned = get(),
            setQuantityUseCase = get(),
            adjustQuantityUseCase = get(),
        )
    }
}
