package com.hayse.sorcery.feature.collection.di

import com.hayse.sorcery.feature.collection.data.repository.CollectionRepositoryImpl
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.collection.domain.usecase.AdjustQuantityUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ImportCuriosaUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveCollectionUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveMissingUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveOwnedForCardUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveSetCompletionUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveSurplusUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.SetQuantityUseCase
import com.hayse.sorcery.feature.collection.ui.viewmodel.CollectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val collectionModule = module {
    single<CollectionRepository> { CollectionRepositoryImpl(collectionDao = get(), cardDao = get()) }

    factory { ObserveCollectionUseCase(get()) }
    factory { ObserveOwnedForCardUseCase(get()) }
    factory { ObserveSetCompletionUseCase(get()) }
    factory { ObserveSurplusUseCase(get()) }
    factory { ObserveMissingUseCase(get()) }
    factory { SetQuantityUseCase(get()) }
    factory { AdjustQuantityUseCase(get()) }
    factory { ImportCuriosaUseCase(get()) }

    viewModel {
        CollectionViewModel(
            cardRepository = get(),
            observeCollection = get(),
            observeCompletion = get(),
            observeSurplus = get(),
            observeMissing = get(),
            setQuantityUseCase = get(),
            adjustQuantity = get(),
            importCuriosa = get(),
        )
    }
}
