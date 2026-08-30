package com.hayse.sorcery.feature.game_tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GameHistoryViewModel(
    gameHistory: GameHistoryRepository,
) : ViewModel() {

    val games: StateFlow<List<GameRecord>> = gameHistory.games
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
