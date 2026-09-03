package com.hayse.sorcery.feature.social.di

import com.hayse.sorcery.feature.cards.data.repository.CardImageResolver
import com.hayse.sorcery.feature.social.data.p2p.DefaultPairingBootstrap
import com.hayse.sorcery.feature.social.data.p2p.NearbyMeshConnector
import com.hayse.sorcery.feature.social.data.p2p.NearbyRoomConnector
import com.hayse.sorcery.feature.social.data.p2p.NearbyTradeConnector
import com.hayse.sorcery.feature.social.data.repository.CardCatalogImpl
import com.hayse.sorcery.feature.social.data.repository.SavedTradeRepositoryImpl
import com.hayse.sorcery.feature.social.data.repository.TradeListRepositoryImpl
import com.hayse.sorcery.feature.social.domain.p2p.MeshConnector
import com.hayse.sorcery.feature.social.domain.p2p.PairingBootstrap
import com.hayse.sorcery.feature.social.domain.p2p.RoomConnector
import com.hayse.sorcery.feature.social.domain.p2p.TradeConnector
import com.hayse.sorcery.feature.social.domain.repository.CardCatalog
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import com.hayse.sorcery.feature.social.domain.usecase.ObserveTradeableUseCase
import com.hayse.sorcery.feature.social.domain.usecase.ObserveWantedUseCase
import com.hayse.sorcery.feature.social.domain.usecase.SetTradeableUseCase
import com.hayse.sorcery.feature.social.domain.usecase.SetWantedUseCase
import com.hayse.sorcery.feature.social.ui.viewmodel.GlobalRoomViewModel
import com.hayse.sorcery.feature.social.ui.viewmodel.RoomViewModel
import com.hayse.sorcery.feature.social.ui.viewmodel.SavedTradesViewModel
import com.hayse.sorcery.feature.social.ui.viewmodel.TradeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val socialModule = module {
    single<TradeListRepository> {
        TradeListRepositoryImpl(
            socialDao = get(),
            cardDao = get(),
            collectionDao = get(),
            imageResolver = get(),
        )
    }

    single<SavedTradeRepository> {
        SavedTradeRepositoryImpl(
            socialDao = get(),
            cardDao = get(),
            imageUri = get<CardImageResolver>()::imageUriForSlugs,
            collectionRepository = get(),
        )
    }

    single<CardCatalog> { CardCatalogImpl(cardDao = get(), imageResolver = get()) }

    factory { ObserveTradeableUseCase(get()) }
    factory { ObserveWantedUseCase(get()) }
    factory { SetTradeableUseCase(get()) }
    factory { SetWantedUseCase(get()) }

    single<TradeConnector> { NearbyTradeConnector(androidContext()) }
    single<PairingBootstrap> { DefaultPairingBootstrap() }
    single<RoomConnector> { NearbyRoomConnector(connector = get()) }
    single<MeshConnector> { NearbyMeshConnector(androidContext()) }

    viewModel {
        TradeViewModel(
            observeTradeable = get(),
            observeWanted = get(),
            setTradeable = get(),
            setWanted = get(),
            cardRepository = get(),
        )
    }

    viewModel {
        RoomViewModel(
            bootstrap = get(),
            connector = get(),
            tradeLists = get(),
            savedTrades = get(),
            collection = get(),
            catalog = get(),
            settings = get(),
        )
    }

    viewModel {
        GlobalRoomViewModel(
            settings = get(),
            meshConnector = get(),
            tradeLists = get(),
            savedTrades = get(),
            collection = get(),
            catalog = get(),
        )
    }

    viewModel { SavedTradesViewModel(repository = get()) }
}
