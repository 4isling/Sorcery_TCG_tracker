package com.hayse.sorcery.feature.game_tracker.domain.model

/** Verdict rendu à la fin de la mort subite, quand aucun avatar n'a été vaincu. */
enum class TimerVerdict { PlayerOneWins, PlayerTwoWins, Draw }

/**
 * Logique pure du chrono : décompte, bascule en mort subite et consommation des tours
 * supplémentaires. Sans effet de bord ni horloge — l'écoulement du temps est fourni par l'appelant.
 */
object GameTimer {

    fun start(config: TimerConfig): GameTimerState = GameTimerState(
        config = config,
        globalRemaining = config.globalSeconds,
        turnRemaining = config.perTurnSeconds,
        running = config.enabled,
        phase = TimerPhase.Normal,
        extraTurnsLeft = 0,
    )

    /**
     * Applique [elapsedSeconds] secondes écoulées. Le minuteur par tour décompte dans toutes les
     * phases actives ; le minuteur global ne décompte qu'en phase [TimerPhase.Normal] et, une fois
     * à zéro, fait basculer en mort subite avec [TimerConfig.extraTurns] tours restants.
     */
    fun tick(state: GameTimerState, elapsedSeconds: Int): GameTimerState {
        if (!state.running || state.phase == TimerPhase.Finished || elapsedSeconds <= 0) return state
        val cfg = state.config
        val newTurn =
            if (cfg.usePerTurn) (state.turnRemaining - elapsedSeconds).coerceAtLeast(0) else state.turnRemaining

        if (state.phase == TimerPhase.SuddenDeath || !cfg.useGlobal) {
            return state.copy(turnRemaining = newTurn)
        }

        val newGlobal = (state.globalRemaining - elapsedSeconds).coerceAtLeast(0)
        if (newGlobal > 0) {
            return state.copy(globalRemaining = newGlobal, turnRemaining = newTurn)
        }
        // Temps global épuisé : la suite dépend de l'action de fin choisie.
        return when (cfg.expiry) {
            // On continue : le global reste à zéro mais le minuteur par tour poursuit.
            TimerExpiryAction.None ->
                state.copy(globalRemaining = 0, turnRemaining = newTurn)

            TimerExpiryAction.SuddenDeath -> if (cfg.extraTurns > 0) {
                state.copy(
                    globalRemaining = 0,
                    turnRemaining = newTurn,
                    phase = TimerPhase.SuddenDeath,
                    extraTurnsLeft = cfg.extraTurns,
                    running = true,
                )
            } else {
                state.copy(globalRemaining = 0, turnRemaining = newTurn, phase = TimerPhase.Finished, running = false)
            }

            TimerExpiryAction.Verdict, TimerExpiryAction.Draw ->
                state.copy(globalRemaining = 0, turnRemaining = newTurn, phase = TimerPhase.Finished, running = false)
        }
    }

    /** Nouveau tour : remet le minuteur de tour à zéro et, en mort subite, consomme un tour. */
    fun onNewTurn(state: GameTimerState): GameTimerState {
        if (state.phase == TimerPhase.Finished) return state
        val resetTurn = state.config.perTurnSeconds
        if (state.phase != TimerPhase.SuddenDeath) {
            return state.copy(turnRemaining = resetTurn)
        }
        val left = (state.extraTurnsLeft - 1).coerceAtLeast(0)
        return if (left == 0) {
            state.copy(extraTurnsLeft = 0, phase = TimerPhase.Finished, running = false, turnRemaining = resetTurn)
        } else {
            state.copy(extraTurnsLeft = left, turnRemaining = resetTurn)
        }
    }

    fun setRunning(state: GameTimerState, running: Boolean): GameTimerState =
        if (state.phase == TimerPhase.Finished) state else state.copy(running = running)

    /**
     * Verdict de fin de mort subite selon la règle de tournoi : les deux joueurs dans le même état
     * (tous deux en vie ou tous deux au seuil de la mort) = nul ; un seul au seuil de la mort =
     * l'autre gagne. Un avatar vaincu compte comme « à terre ».
     */
    fun verdict(one: AvatarStatus, two: AvatarStatus): TimerVerdict {
        val oneDown = one != AvatarStatus.Active
        val twoDown = two != AvatarStatus.Active
        return when {
            oneDown && !twoDown -> TimerVerdict.PlayerTwoWins
            twoDown && !oneDown -> TimerVerdict.PlayerOneWins
            else -> TimerVerdict.Draw
        }
    }
}
