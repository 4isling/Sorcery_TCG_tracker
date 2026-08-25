package com.hayse.sorcery.feature.game_tracker.data.local.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerState
import kotlinx.serialization.Serializable

/**
 * Représentation persistée de l'état de partie (DataStore). L'historique n'est pas persisté :
 * on reprend la partie en cours après un process death, mais l'undo ne remonte pas avant le redémarrage.
 */
@Serializable
data class GameStateData(
    val startingLife: Int,
    val players: List<PlayerStateData>,
    val turn: Int,
    val activePlayerIndex: Int,
)

@Serializable
data class PlayerStateData(
    val life: Int,
    val status: String,
    val sitesControlled: Int,
    val manaAvailable: Int,
    val affinity: Map<String, Int>,
)

fun GameState.toData(): GameStateData = GameStateData(
    startingLife = config.startingLife,
    players = listOf(player(PlayerId.One).toData(), player(PlayerId.Two).toData()),
    turn = turn,
    activePlayerIndex = if (activePlayer == PlayerId.One) 0 else 1,
)

fun GameStateData.toDomain(): GameState = GameState(
    config = GameConfig(startingLife = startingLife),
    players = mapOf(
        PlayerId.One to players[0].toDomain(),
        PlayerId.Two to players[1].toDomain(),
    ),
    turn = turn,
    activePlayer = if (activePlayerIndex == 0) PlayerId.One else PlayerId.Two,
    history = emptyList(),
)

private fun PlayerState.toData(): PlayerStateData = PlayerStateData(
    life = life,
    status = avatarStatus.name,
    sitesControlled = sitesControlled,
    manaAvailable = manaAvailable,
    affinity = affinity.entries.associate { (element, value) -> element.name to value },
)

private fun PlayerStateData.toDomain(): PlayerState = PlayerState(
    life = life,
    avatarStatus = AvatarStatus.valueOf(status),
    sitesControlled = sitesControlled,
    manaAvailable = manaAvailable,
    affinity = Element.entries.associateWith { element -> affinity[element.name] ?: 0 },
)
