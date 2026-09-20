package com.hayse.sorcery.feature.game_tracker.domain.model

/** Phase de vie du chrono : jeu normal, mort subite (tours supplémentaires), puis terminé. */
enum class TimerPhase { Normal, SuddenDeath, Finished }

/**
 * État courant du chrono d'une partie. Découplé de [GameState] : l'undo rejoue les événements
 * depuis le début et ne doit surtout pas remonter l'horloge, donc le chrono vit dans son propre
 * store.
 */
data class GameTimerState(
    val config: TimerConfig,
    val globalRemaining: Int,
    val turnRemaining: Int,
    val running: Boolean,
    val phase: TimerPhase,
    val extraTurnsLeft: Int,
)
