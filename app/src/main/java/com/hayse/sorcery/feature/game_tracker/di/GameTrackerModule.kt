package com.hayse.sorcery.feature.game_tracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.hayse.sorcery.feature.game_tracker.data.local.GameStateSerializer
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameStateData
import com.hayse.sorcery.feature.game_tracker.data.repository.GameSessionRepositoryImpl
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameSessionRepository
import com.hayse.sorcery.feature.game_tracker.domain.usecase.AdjustManaUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyDamageUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyLifeGainUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyLifeLossUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ResetGameUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.StartNewTurnUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UndoLastEventUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UpdateAffinityUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UpdateSiteCountUseCase
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameTrackerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val gameTrackerModule = module {
    single<DataStore<GameStateData?>> {
        DataStoreFactory.create(
            serializer = GameStateSerializer,
            produceFile = { androidContext().dataStoreFile("game_state.json") },
        )
    }
    single<GameSessionRepository> { GameSessionRepositoryImpl(get()) }

    factory { ApplyDamageUseCase() }
    factory { ApplyLifeLossUseCase() }
    factory { ApplyLifeGainUseCase() }
    factory { AdjustManaUseCase() }
    factory { UpdateSiteCountUseCase() }
    factory { UpdateAffinityUseCase() }
    factory { StartNewTurnUseCase() }
    factory { UndoLastEventUseCase() }
    factory { ResetGameUseCase() }

    viewModel {
        GameTrackerViewModel(
            repository = get(),
            applyDamage = get(),
            applyLifeLoss = get(),
            applyLifeGain = get(),
            adjustMana = get(),
            updateSites = get(),
            updateAffinity = get(),
            startNewTurn = get(),
            undoLast = get(),
            resetGame = get(),
        )
    }
}
