package com.hayse.sorcery.feature.game_tracker.domain.usecase

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.GameReducer
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameEvent
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.DiceRollPurpose
import com.hayse.sorcery.feature.game_tracker.domain.record
import kotlin.random.Random

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

/** Lanceur générique : [count] dés à [faces] faces. Le hasard vit ici (hors reducer). */
class RollDiceUseCase(private val random: Random = Random) {
    operator fun invoke(state: GameState, count: Int, faces: Int): GameState {
        val safeCount = count.coerceAtLeast(1)
        val safeFaces = faces.coerceAtLeast(2)
        val results = List(safeCount) { random.nextInt(1, safeFaces + 1) }
        return state.record(GameEvent.DiceRoll(faces = safeFaces, results = results))
    }
}

/** Tire un d2 pour désigner le joueur qui commence, et bascule le joueur actif dessus. */
class RollForFirstPlayerUseCase(private val random: Random = Random) {
    operator fun invoke(state: GameState): GameState {
        val roll = random.nextInt(1, 3)
        val chosen = if (roll == 1) PlayerId.One else PlayerId.Two
        return state.record(GameEvent.FirstPlayerRoll(results = listOf(roll), chosen = chosen))
    }
}

/** Setup du passif Harbinger : 3 jets de d20 pour choisir les cases. */
class RollHarbingerUseCase(private val random: Random = Random) {
    operator fun invoke(state: GameState): GameState {
        val results = List(3) { random.nextInt(1, 21) }
        return state.record(GameEvent.DiceRoll(faces = 20, results = results, purpose = DiceRollPurpose.Harbinger))
    }
}
