package com.hayse.sorcery.feature.game_tracker.domain.model

data class GameState(
    val config: GameConfig,
    val players: Map<PlayerId, PlayerState>,
    val turn: Int,
    val activePlayer: PlayerId,
    val history: List<GameEvent>,
) {
    fun player(id: PlayerId): PlayerState = players.getValue(id)

    fun withPlayer(id: PlayerId, transform: (PlayerState) -> PlayerState): GameState =
        copy(players = players + (id to transform(player(id))))

    companion object {
        fun initial(config: GameConfig = GameConfig()): GameState {
            val start = PlayerState(life = config.startingLife)
            return GameState(
                config = config,
                players = mapOf(PlayerId.One to start, PlayerId.Two to start),
                turn = 1,
                activePlayer = PlayerId.One,
                history = emptyList(),
            )
        }
    }
}
