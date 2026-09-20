package com.hayse.sorcery.feature.game_tracker.data.local.model

import com.hayse.sorcery.feature.game_tracker.domain.model.GameTimerState
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerExpiryAction
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerPhase
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameTimerSnapshot
import kotlinx.serialization.Serializable

/** Chrono persisté à plat (config + état courant + horodatage du dernier tick). */
@Serializable
data class GameTimerData(
    val enabled: Boolean = false,
    val useGlobal: Boolean = true,
    val globalSeconds: Int = 50 * 60,
    val usePerTurn: Boolean = false,
    val perTurnSeconds: Int = 5 * 60,
    val extraTurns: Int = 3,
    val expiry: TimerExpiryAction = TimerExpiryAction.SuddenDeath,
    val globalRemaining: Int = 50 * 60,
    val turnRemaining: Int = 5 * 60,
    val running: Boolean = false,
    val phase: TimerPhase = TimerPhase.Normal,
    val extraTurnsLeft: Int = 0,
    val lastTickEpochMs: Long = 0L,
)

fun GameTimerSnapshot.toData(): GameTimerData = GameTimerData(
    enabled = state.config.enabled,
    useGlobal = state.config.useGlobal,
    globalSeconds = state.config.globalSeconds,
    usePerTurn = state.config.usePerTurn,
    perTurnSeconds = state.config.perTurnSeconds,
    extraTurns = state.config.extraTurns,
    expiry = state.config.expiry,
    globalRemaining = state.globalRemaining,
    turnRemaining = state.turnRemaining,
    running = state.running,
    phase = state.phase,
    extraTurnsLeft = state.extraTurnsLeft,
    lastTickEpochMs = lastTickEpochMs,
)

fun GameTimerData.toDomain(): GameTimerSnapshot = GameTimerSnapshot(
    state = GameTimerState(
        config = TimerConfig(
            enabled = enabled,
            useGlobal = useGlobal,
            globalSeconds = globalSeconds,
            usePerTurn = usePerTurn,
            perTurnSeconds = perTurnSeconds,
            extraTurns = extraTurns,
            expiry = expiry,
        ),
        globalRemaining = globalRemaining,
        turnRemaining = turnRemaining,
        running = running,
        phase = phase,
        extraTurnsLeft = extraTurnsLeft,
    ),
    lastTickEpochMs = lastTickEpochMs,
)
