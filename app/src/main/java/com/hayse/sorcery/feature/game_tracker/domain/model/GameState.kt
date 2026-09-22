package com.hayse.sorcery.feature.game_tracker.domain.model

data class GameState(
    val config: GameConfig,
    val players: Map<PlayerId, PlayerState>,
    val turn: Int,
    val activePlayer: PlayerId,
    val history: List<GameEvent>,
    /** Horodatage (epoch ms) du début de partie ; 0 si inconnu (parties antérieures au suivi). */
    val startedAt: Long = 0L,
) {
    fun player(id: PlayerId): PlayerState = players.getValue(id)

    fun withPlayer(id: PlayerId, transform: (PlayerState) -> PlayerState): GameState =
        copy(players = players + (id to transform(player(id))))

    companion object {
        fun initial(config: GameConfig = GameConfig()): GameState = GameState(
            config = config,
            players = mapOf(
                PlayerId.One to newPlayer(config.startingLife, config.playerOne),
                PlayerId.Two to newPlayer(config.startingLife, config.playerTwo),
            ),
            turn = 1,
            activePlayer = PlayerId.One,
            history = emptyList(),
        )

        private fun newPlayer(life: Int, identity: PlayerIdentity?): PlayerState = PlayerState(
            life = life,
            avatarName = identity?.avatarName,
            avatarImageUri = identity?.avatarImageUri,
            pseudo = identity?.pseudo,
        )
    }
}
