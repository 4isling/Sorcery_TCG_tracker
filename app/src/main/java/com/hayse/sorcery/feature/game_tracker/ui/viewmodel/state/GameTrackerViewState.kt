package com.hayse.sorcery.feature.game_tracker.ui.viewmodel.state

import com.hayse.sorcery.feature.game_tracker.domain.model.GameState

data class GameTrackerViewState(
    val game: GameState? = null,
    val loading: Boolean = true,
    val showLog: Boolean = false,
    val showDice: Boolean = false,
    /** Pseudo par défaut du joueur 1, pour pré-remplir la nouvelle partie. */
    val defaultOwnerPseudo: String? = null,
)
