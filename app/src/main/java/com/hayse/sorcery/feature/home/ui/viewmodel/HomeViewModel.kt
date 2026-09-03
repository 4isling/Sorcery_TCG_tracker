package com.hayse.sorcery.feature.home.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameHistoryRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameSessionRepository
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeViewState(
    val loading: Boolean = true,
    val hasActiveGame: Boolean = false,
    val activeTurn: Int? = null,
    val recentGames: List<GameRecord> = emptyList(),
    val socialEnabled: Boolean = true,
)

class HomeViewModel(
    gameSession: GameSessionRepository,
    gameHistory: GameHistoryRepository,
    settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<HomeViewState> = combine(
        gameSession.gameState,
        gameHistory.games,
        settings.settings,
    ) { game, history, appSettings ->
        HomeViewState(
            loading = false,
            hasActiveGame = game != null,
            activeTurn = game?.turn,
            recentGames = history.take(3),
            socialEnabled = appSettings.socialEnabled,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeViewState())
}
