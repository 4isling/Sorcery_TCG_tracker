package com.hayse.sorcery.feature.deck.di

import com.hayse.sorcery.feature.deck.data.repository.DeckRepositoryImpl
import com.hayse.sorcery.feature.deck.domain.repository.DeckRepository
import com.hayse.sorcery.feature.deck.domain.usecase.AdjustDeckCardQuantityUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.CreateDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.DeleteDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.ObserveDeckCatalogUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.ObserveDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.ObserveDecksUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.RenameDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.SetDeckCardQuantityUseCase
import com.hayse.sorcery.feature.deck.ui.viewmodel.DeckEditorViewModel
import com.hayse.sorcery.feature.deck.ui.viewmodel.DeckListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val deckModule = module {
    single<DeckRepository> {
        DeckRepositoryImpl(deckDao = get(), cardDao = get(), collectionDao = get(), imageResolver = get())
    }

    factory { ObserveDecksUseCase(get()) }
    factory { ObserveDeckUseCase(get()) }
    factory { ObserveDeckCatalogUseCase(get()) }
    factory { CreateDeckUseCase(get()) }
    factory { RenameDeckUseCase(get()) }
    factory { DeleteDeckUseCase(get()) }
    factory { SetDeckCardQuantityUseCase(get()) }
    factory { AdjustDeckCardQuantityUseCase(get()) }

    viewModel {
        DeckListViewModel(
            observeDecks = get(),
            createDeck = get(),
            deleteDeck = get(),
        )
    }
    viewModel { (deckId: Long) ->
        DeckEditorViewModel(
            deckId = deckId,
            repository = get(),
            observeDeck = get(),
            observeCatalog = get(),
            renameDeck = get(),
            setCard = get(),
            adjustCard = get(),
        )
    }
}
