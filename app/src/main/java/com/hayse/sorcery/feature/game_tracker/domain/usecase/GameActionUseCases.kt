package com.hayse.sorcery.feature.game_tracker.domain.usecase

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.GameReducer
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameEvent
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.record

class ApplyDamageUseCase {
    operator fun invoke(state: GameState, player: PlayerId, amount: Int): GameState =
        state.record(GameEvent.Damage(player, amount))
}

class ApplyLifeLossUseCase {
    operator fun invoke(state: GameState, player: PlayerId, amount: Int): GameState =
        state.record(GameEvent.LifeLoss(player, amount))
}

class ApplyLifeGainUseCase {
    operator fun invoke(state: GameState, player: PlayerId, amount: Int): GameState =
        state.record(GameEvent.LifeGain(player, amount))
}

class AdjustManaUseCase {
    operator fun invoke(state: GameState, player: PlayerId, delta: Int): GameState =
        state.record(GameEvent.ManaAdjust(player, delta))
}

class UpdateSiteCountUseCase {
    operator fun invoke(state: GameState, player: PlayerId, delta: Int): GameState =
        state.record(GameEvent.SiteCountChange(player, delta))
}

class UpdateAffinityUseCase {
    operator fun invoke(state: GameState, player: PlayerId, element: Element, delta: Int): GameState =
        state.record(GameEvent.AffinityChange(player, element, delta))
}

class StartNewTurnUseCase {
    operator fun invoke(state: GameState): GameState =
        state.record(GameEvent.NewTurn)
}

class UndoLastEventUseCase {
    operator fun invoke(state: GameState): GameState {
        if (state.history.isEmpty()) return state
        val remaining = state.history.dropLast(1)
        val base = GameState.initial(state.config)
        val replayed = remaining.fold(base) { acc, event -> GameReducer.apply(acc, event) }
        return replayed.copy(history = remaining)
    }
}

class ResetGameUseCase {
    operator fun invoke(config: GameConfig = GameConfig()): GameState = GameState.initial(config)
}
