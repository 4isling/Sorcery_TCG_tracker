package com.hayse.sorcery.feature.game_tracker.ui.viewmodel.state

import com.hayse.sorcery.feature.game_tracker.domain.model.GameState

data class GameTrackerViewState(
    val game: GameState? = null,
    val loading: Boolean = true,
)
