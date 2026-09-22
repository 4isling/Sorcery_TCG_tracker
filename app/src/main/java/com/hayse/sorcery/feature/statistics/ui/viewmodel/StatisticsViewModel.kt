package com.hayse.sorcery.feature.statistics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameHistoryRepository
import com.hayse.sorcery.feature.statistics.data.repository.GameStatsComputations
import com.hayse.sorcery.feature.statistics.domain.model.CollectionStats
import com.hayse.sorcery.feature.statistics.domain.model.GameStats
import com.hayse.sorcery.feature.statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class StatisticsTab { Collection, Games }

class StatisticsViewModel(
    statisticsRepository: StatisticsRepository,
    gameHistory: GameHistoryRepository,
) : ViewModel() {

    val collection: StateFlow<CollectionStats> = statisticsRepository.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionStats())

    val games: StateFlow<GameStats> = gameHistory.games
        .map(GameStatsComputations::compute)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameStats())
}
