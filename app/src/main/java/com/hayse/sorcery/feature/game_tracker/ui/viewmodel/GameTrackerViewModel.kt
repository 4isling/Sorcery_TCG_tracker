package com.hayse.sorcery.feature.game_tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
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
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.state.GameTrackerViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameTrackerViewModel(
    private val repository: GameSessionRepository,
    private val applyDamage: ApplyDamageUseCase,
    private val applyLifeLoss: ApplyLifeLossUseCase,
    private val applyLifeGain: ApplyLifeGainUseCase,
    private val adjustMana: AdjustManaUseCase,
    private val updateSites: UpdateSiteCountUseCase,
    private val updateAffinity: UpdateAffinityUseCase,
    private val startNewTurn: StartNewTurnUseCase,
    private val undoLast: UndoLastEventUseCase,
    private val resetGame: ResetGameUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GameTrackerViewState())
    val state: StateFlow<GameTrackerViewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val persisted = repository.gameState.first()
            _state.value = GameTrackerViewState(game = persisted, loading = false)
        }
    }

    fun newGame(config: GameConfig = GameConfig()) = commit(resetGame(config))

    fun endGame() {
        _state.value = _state.value.copy(game = null)
        viewModelScope.launch { repository.clear() }
    }

    fun damage(player: PlayerId, amount: Int) = mutate { applyDamage(it, player, amount) }
    fun lifeLoss(player: PlayerId, amount: Int) = mutate { applyLifeLoss(it, player, amount) }
    fun lifeGain(player: PlayerId, amount: Int) = mutate { applyLifeGain(it, player, amount) }
    fun manaDelta(player: PlayerId, delta: Int) = mutate { adjustMana(it, player, delta) }
    fun siteDelta(player: PlayerId, delta: Int) = mutate { updateSites(it, player, delta) }
    fun affinityDelta(player: PlayerId, element: Element, delta: Int) =
        mutate { updateAffinity(it, player, element, delta) }

    fun newTurn() = mutate { startNewTurn(it) }
    fun undo() = mutate { undoLast(it) }

    private fun mutate(transform: (GameState) -> GameState) {
        val current = _state.value.game ?: return
        commit(transform(current))
    }

    private fun commit(game: GameState) {
        _state.value = _state.value.copy(game = game, loading = false)
        viewModelScope.launch { repository.save(game) }
    }
}
