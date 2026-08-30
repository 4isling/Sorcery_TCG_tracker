package com.hayse.sorcery.feature.game_tracker.domain.model

/** Trace d'une partie terminée, affichée dans l'historique des parties. */
data class GameRecord(
    val playedAt: Long,
    val playerOnePseudo: String?,
    val playerOneAvatar: String?,
    val playerTwoPseudo: String?,
    val playerTwoAvatar: String?,
    val winner: PlayerId?,
    val turns: Int,
)
