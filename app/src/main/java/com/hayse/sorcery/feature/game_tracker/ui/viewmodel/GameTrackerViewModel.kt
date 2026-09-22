package com.hayse.sorcery.feature.game_tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.GameTimer
import com.hayse.sorcery.feature.game_tracker.domain.model.GameTimerState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerExpiryAction
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerPhase
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerVerdict
import com.hayse.sorcery.feature.game_tracker.domain.model.outcome
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameHistoryRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameSessionRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameTimerRepository
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameTimerSnapshot
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameTrackerViewModel(
    private val repository: GameSessionRepository,
    private val gameHistory: GameHistoryRepository,
    private val playerPrefs: PlayerPrefsRepository,
    private val gameTimer: GameTimerRepository,
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

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            val persisted = repository.gameState.first()
            val ownerPseudo = playerPrefs.ownerPseudo.first()
            val timer = reconcileTimer(gameTimer.snapshot.first(), persisted)
            val verdict = if (timer?.phase == TimerPhase.Finished && persisted != null) {
                expiryVerdict(timer, persisted)
            } else {
                null
            }
            _state.value = _state.value.copy(
                game = persisted,
                loading = false,
                defaultOwnerPseudo = ownerPseudo,
                timer = timer,
                verdict = verdict,
            )
            refreshAvatarSets()
            ensureTicking()
        }
    }

    /** Rejoue le temps écoulé (horloge murale) pendant que l'app était fermée, puis re-persiste. */
    private suspend fun reconcileTimer(
        snapshot: GameTimerSnapshot?,
        game: GameState?,
    ): GameTimerState? {
        if (snapshot == null || game == null) return null
        var st = snapshot.state
        if (st.running && st.phase != TimerPhase.Finished && !game.hasDefeated()) {
            val elapsed = ((System.currentTimeMillis() - snapshot.lastTickEpochMs) / 1000).toInt()
            if (elapsed > 0) st = GameTimer.tick(st, elapsed)
        }
        gameTimer.save(GameTimerSnapshot(st, System.currentTimeMillis()))
        return st
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

    fun newGame(config: GameConfig = GameConfig(), timerConfig: TimerConfig = TimerConfig.Disabled) {
        config.playerOne?.pseudo?.let { pseudo ->
            viewModelScope.launch { playerPrefs.setOwnerPseudo(pseudo) }
            _state.value = _state.value.copy(defaultOwnerPseudo = pseudo)
        }
        commit(resetGame(config).copy(startedAt = System.currentTimeMillis()))
        refreshAvatarSets()

        val timer = if (timerConfig.enabled) GameTimer.start(timerConfig) else null
        _state.value = _state.value.copy(timer = timer, verdict = null)
        viewModelScope.launch {
            if (timer != null) gameTimer.save(GameTimerSnapshot(timer, System.currentTimeMillis()))
            else gameTimer.clear()
        }
        ensureTicking()
    }

    fun endGame() {
        _state.value.game?.let { game ->
            val outcome = game.outcome()
            val one = game.player(PlayerId.One)
            val two = game.player(PlayerId.Two)
            val now = System.currentTimeMillis()
            val duration = if (game.startedAt > 0L) ((now - game.startedAt) / 1000).toInt() else null
            val record = GameRecord(
                playedAt = now,
                playerOnePseudo = one.pseudo,
                playerOneAvatar = one.avatarName,
                playerTwoPseudo = two.pseudo,
                playerTwoAvatar = two.avatarName,
                winner = outcome.winner,
                turns = outcome.turns,
                durationSeconds = duration,
            )
            viewModelScope.launch { gameHistory.add(record) }
        }
        _state.value = _state.value.copy(game = null, timer = null, verdict = null)
        viewModelScope.launch {
            repository.clear()
            gameTimer.clear()
        }
    }

    fun damage(player: PlayerId, amount: Int) = mutate { applyDamage(it, player, amount) }
    fun lifeLoss(player: PlayerId, amount: Int) = mutate { applyLifeLoss(it, player, amount) }
    fun lifeGain(player: PlayerId, amount: Int) = mutate { applyLifeGain(it, player, amount) }
    fun manaDelta(player: PlayerId, delta: Int) = mutate { adjustMana(it, player, delta) }
    fun siteDelta(player: PlayerId, delta: Int) = mutate { updateSites(it, player, delta) }
    fun affinityDelta(player: PlayerId, element: Element, delta: Int) =
        mutate { updateAffinity(it, player, element, delta) }

    fun newTurn() {
        mutate { startNewTurn(it) }
        val st = _state.value.timer ?: return
        val next = GameTimer.onNewTurn(st)
        _state.value = _state.value.copy(timer = next, verdict = verdictFor(next, st))
        persistTimer(next)
    }

    fun undo() = mutate { undoLast(it) }

    /** Bascule pause/reprise du chrono. */
    fun toggleTimer() {
        val st = _state.value.timer ?: return
        val next = GameTimer.setRunning(st, !st.running)
        _state.value = _state.value.copy(timer = next)
        persistTimer(next)
    }

    fun dismissVerdict() {
        _state.value = _state.value.copy(verdict = null)
    }

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

    /**
     * Boucle unique de décompte, une seconde à la fois. Ne persiste qu'aux changements de phase :
     * entre deux, l'affichage suffit et le rattrapage horloge murale garantit l'exactitude au retour.
     */
    private fun ensureTicking() {
        if (tickJob?.isActive == true) return
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                tickOnce()
            }
        }
    }

    private fun tickOnce() {
        val st = _state.value.timer ?: return
        val game = _state.value.game ?: return
        if (!st.running || st.phase == TimerPhase.Finished || game.hasDefeated()) return
        val next = GameTimer.tick(st, 1)
        _state.value = _state.value.copy(timer = next, verdict = verdictFor(next, st))
        if (next.phase != st.phase) persistTimer(next)
    }

    /** Verdict figé quand le chrono se termine, selon l'action de fin choisie. */
    private fun verdictFor(next: GameTimerState, prev: GameTimerState): TimerVerdict? {
        val game = _state.value.game ?: return _state.value.verdict
        return if (next.phase == TimerPhase.Finished && prev.phase != TimerPhase.Finished) {
            expiryVerdict(next, game)
        } else {
            _state.value.verdict
        }
    }

    /** Verdict à présenter selon [TimerConfig.expiry] ; null quand aucune conclusion automatique. */
    private fun expiryVerdict(timer: GameTimerState, game: GameState): TimerVerdict? =
        when (timer.config.expiry) {
            TimerExpiryAction.None -> null
            TimerExpiryAction.Draw -> TimerVerdict.Draw
            TimerExpiryAction.Verdict, TimerExpiryAction.SuddenDeath -> GameTimer.verdict(
                game.player(PlayerId.One).avatarStatus,
                game.player(PlayerId.Two).avatarStatus,
            )
        }

    private fun persistTimer(st: GameTimerState) {
        viewModelScope.launch { gameTimer.save(GameTimerSnapshot(st, System.currentTimeMillis())) }
    }

    private fun GameState.hasDefeated(): Boolean =
        players.values.any { it.avatarStatus == AvatarStatus.Defeated }
}
