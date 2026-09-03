package com.hayse.sorcery.feature.game_tracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.hayse.sorcery.feature.game_tracker.data.local.GameHistorySerializer
import com.hayse.sorcery.feature.game_tracker.data.local.GameStateSerializer
import com.hayse.sorcery.feature.game_tracker.data.local.PlayerPrefsSerializer
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameHistoryData
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameStateData
import com.hayse.sorcery.feature.game_tracker.data.local.model.PlayerPrefsData
import com.hayse.sorcery.feature.game_tracker.data.repository.GameHistoryRepositoryImpl
import com.hayse.sorcery.feature.game_tracker.data.repository.GameSessionRepositoryImpl
import com.hayse.sorcery.feature.game_tracker.data.repository.PlayerPrefsRepositoryImpl
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameHistoryRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameSessionRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.PlayerPrefsRepository
import com.hayse.sorcery.feature.game_tracker.domain.usecase.AdjustManaUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyDamageUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyLifeGainUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyLifeLossUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ResetGameUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.RollDiceUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.RollForFirstPlayerUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.RollHarbingerUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.StartNewTurnUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UndoLastEventUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UpdateAffinityUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UpdateSiteCountUseCase
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.AvatarPickerViewModel
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameHistoryViewModel
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameTrackerViewModel
import com.hayse.sorcery.feature.home.ui.viewmodel.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Koin indexe les singles par classe effacée : les trois DataStore partagent la même classe
// (DataStore) et se masqueraient sans qualifiers distincts.
private val gameStateStore = named("game_state_store")
private val gameHistoryStore = named("game_history_store")
private val playerPrefsStore = named("player_prefs_store")

val gameTrackerModule = module {
    single<DataStore<GameStateData?>>(gameStateStore) {
        DataStoreFactory.create(
            serializer = GameStateSerializer,
            produceFile = { androidContext().dataStoreFile("game_state.json") },
        )
    }
    single<DataStore<GameHistoryData>>(gameHistoryStore) {
        DataStoreFactory.create(
            serializer = GameHistorySerializer,
            produceFile = { androidContext().dataStoreFile("game_history.json") },
        )
    }
    single<DataStore<PlayerPrefsData>>(playerPrefsStore) {
        DataStoreFactory.create(
            serializer = PlayerPrefsSerializer,
            produceFile = { androidContext().dataStoreFile("player_prefs.json") },
        )
    }

    single<GameSessionRepository> { GameSessionRepositoryImpl(get(gameStateStore)) }
    single<GameHistoryRepository> { GameHistoryRepositoryImpl(get(gameHistoryStore)) }
    single<PlayerPrefsRepository> { PlayerPrefsRepositoryImpl(get(playerPrefsStore)) }

    factory { ApplyDamageUseCase() }
    factory { ApplyLifeLossUseCase() }
    factory { ApplyLifeGainUseCase() }
    factory { AdjustManaUseCase() }
    factory { UpdateSiteCountUseCase() }
    factory { UpdateAffinityUseCase() }
    factory { StartNewTurnUseCase() }
    factory { UndoLastEventUseCase() }
    factory { ResetGameUseCase() }
    factory { RollDiceUseCase() }
    factory { RollForFirstPlayerUseCase() }
    factory { RollHarbingerUseCase() }

    viewModel {
        GameTrackerViewModel(
            repository = get(),
            gameHistory = get(),
            playerPrefs = get(),
            cardRepository = get(),
            applyDamage = get(),
            applyLifeLoss = get(),
            applyLifeGain = get(),
            adjustMana = get(),
            updateSites = get(),
            updateAffinity = get(),
            startNewTurn = get(),
            undoLast = get(),
            resetGame = get(),
            rollDiceUseCase = get(),
            rollFirstPlayerUseCase = get(),
            rollHarbingerUseCase = get(),
        )
    }
    viewModel { GameHistoryViewModel(get()) }
    viewModel { AvatarPickerViewModel(get()) }
    viewModel { HomeViewModel(gameSession = get(), gameHistory = get(), settings = get()) }
}
