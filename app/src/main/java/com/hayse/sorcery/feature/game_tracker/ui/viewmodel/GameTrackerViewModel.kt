package com.hayse.sorcery.feature.game_tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.outcome
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
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.state.GameTrackerViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameTrackerViewModel(
    private val repository: GameSessionRepository,
    private val gameHistory: GameHistoryRepository,
    private val playerPrefs: PlayerPrefsRepository,
    private val cardRepository: CardRepository,
    private val applyDamage: ApplyDamageUseCase,
    private val applyLifeLoss: ApplyLifeLossUseCase,
    private val applyLifeGain: ApplyLifeGainUseCase,
    private val adjustMana: AdjustManaUseCase,
    private val updateSites: UpdateSiteCountUseCase,
    private val updateAffinity: UpdateAffinityUseCase,
    private val startNewTurn: StartNewTurnUseCase,
    private val undoLast: UndoLastEventUseCase,
    private val resetGame: ResetGameUseCase,
    private val rollDiceUseCase: RollDiceUseCase,
    private val rollFirstPlayerUseCase: RollForFirstPlayerUseCase,
    private val rollHarbingerUseCase: RollHarbingerUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GameTrackerViewState())
    val state: StateFlow<GameTrackerViewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val persisted = repository.gameState.first()
            val ownerPseudo = playerPrefs.ownerPseudo.first()
            _state.value = _state.value.copy(
                game = persisted,
                loading = false,
                defaultOwnerPseudo = ownerPseudo,
            )
            refreshAvatarSets()
        }
    }

    /** Résout le set de l'avatar de chaque joueur (change uniquement au démarrage d'une partie). */
    private fun refreshAvatarSets() {
        val game = _state.value.game
        viewModelScope.launch {
            _state.value = _state.value.copy(
                playerOneAvatarSet = avatarSetName(game?.player(PlayerId.One)?.avatarName),
                playerTwoAvatarSet = avatarSetName(game?.player(PlayerId.Two)?.avatarName),
            )
        }
    }

    private suspend fun avatarSetName(avatarName: String?): String? =
        avatarName?.let { cardRepository.getCard(it)?.printings?.firstOrNull()?.setName }

    fun newGame(config: GameConfig = GameConfig()) {
        config.playerOne?.pseudo?.let { pseudo ->
            viewModelScope.launch { playerPrefs.setOwnerPseudo(pseudo) }
            _state.value = _state.value.copy(defaultOwnerPseudo = pseudo)
        }
        commit(resetGame(config))
        refreshAvatarSets()
    }

    fun endGame() {
        _state.value.game?.let { game ->
            val outcome = game.outcome()
            val one = game.player(PlayerId.One)
            val two = game.player(PlayerId.Two)
            val record = GameRecord(
                playedAt = System.currentTimeMillis(),
                playerOnePseudo = one.pseudo,
                playerOneAvatar = one.avatarName,
                playerTwoPseudo = two.pseudo,
                playerTwoAvatar = two.avatarName,
                winner = outcome.winner,
                turns = outcome.turns,
            )
            viewModelScope.launch { gameHistory.add(record) }
        }
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

    fun rollDice(count: Int, faces: Int) = mutate { rollDiceUseCase(it, count, faces) }
    fun rollFirstPlayer() = mutate { rollFirstPlayerUseCase(it) }
    fun rollHarbinger() = mutate { rollHarbingerUseCase(it) }

    fun showDice(show: Boolean) {
        _state.value = _state.value.copy(showDice = show)
    }

    fun showLog(show: Boolean) {
        _state.value = _state.value.copy(showLog = show)
    }

    private fun mutate(transform: (GameState) -> GameState) {
        val current = _state.value.game ?: return
        commit(transform(current))
    }

    private fun commit(game: GameState) {
        _state.value = _state.value.copy(game = game, loading = false)
        viewModelScope.launch { repository.save(game) }
    }
}
