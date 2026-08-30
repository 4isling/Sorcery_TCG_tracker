package com.hayse.sorcery.feature.game_tracker.domain.model

/** Paramètres de départ d'une partie. En Sorcery, la vie de départ de l'avatar est de 20. */
data class GameConfig(
    val startingLife: Int = 20,
    val playerOne: PlayerIdentity? = null,
    val playerTwo: PlayerIdentity? = null,
)
