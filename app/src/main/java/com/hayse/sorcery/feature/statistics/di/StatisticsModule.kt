package com.hayse.sorcery.feature.statistics.di

import com.hayse.sorcery.feature.statistics.data.repository.StatisticsRepositoryImpl
import com.hayse.sorcery.feature.statistics.domain.repository.StatisticsRepository
import com.hayse.sorcery.feature.statistics.ui.viewmodel.StatisticsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val statisticsModule = module {
    single<StatisticsRepository> { StatisticsRepositoryImpl(cardDao = get(), collectionDao = get()) }

    viewModel { StatisticsViewModel(statisticsRepository = get(), gameHistory = get()) }
}
