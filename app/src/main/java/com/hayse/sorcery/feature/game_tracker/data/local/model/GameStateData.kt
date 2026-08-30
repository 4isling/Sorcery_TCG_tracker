package com.hayse.sorcery.feature.game_tracker.data.local.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerIdentity
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerState
import kotlinx.serialization.Serializable

/**
 * Représentation persistée de l'état de partie (DataStore). Les nouveaux champs ont des valeurs par
 * défaut pour rester rétro-compatibles avec les JSON écrits par les versions précédentes.
 */
@Serializable
data class GameStateData(
    val startingLife: Int,
    val players: List<PlayerStateData>,
    val turn: Int,
    val activePlayerIndex: Int,
    val history: List<GameEventData> = emptyList(),
)

@Serializable
data class PlayerStateData(
    val life: Int,
    val status: String,
    val sitesControlled: Int,
    val manaAvailable: Int,
    val affinity: Map<String, Int>,
    val avatarName: String? = null,
    val avatarImageUri: String? = null,
    val pseudo: String? = null,
)

fun GameState.toData(): GameStateData = GameStateData(
    startingLife = config.startingLife,
    players = listOf(player(PlayerId.One).toData(), player(PlayerId.Two).toData()),
    turn = turn,
    activePlayerIndex = if (activePlayer == PlayerId.One) 0 else 1,
    history = history.map { it.toData() },
)

fun GameStateData.toDomain(): GameState {
    val one = players[0].toDomain()
    val two = players[1].toDomain()
    return GameState(
        // L'identité vit dans les joueurs ; on la reflète dans la config pour que l'undo (replay
        // depuis initial(config)) reconstruise des joueurs identiques.
        config = GameConfig(
            startingLife = startingLife,
            playerOne = one.identity(),
            playerTwo = two.identity(),
        ),
        players = mapOf(PlayerId.One to one, PlayerId.Two to two),
        turn = turn,
        activePlayer = if (activePlayerIndex == 0) PlayerId.One else PlayerId.Two,
        history = history.map { it.toDomain() },
    )
}

private fun PlayerState.identity(): PlayerIdentity? =
    if (avatarName == null && avatarImageUri == null && pseudo == null) null
    else PlayerIdentity(avatarName, avatarImageUri, pseudo)

private fun PlayerState.toData(): PlayerStateData = PlayerStateData(
    life = life,
    status = avatarStatus.name,
    sitesControlled = sitesControlled,
    manaAvailable = manaAvailable,
    affinity = affinity.entries.associate { (element, value) -> element.name to value },
    avatarName = avatarName,
    avatarImageUri = avatarImageUri,
    pseudo = pseudo,
)

private fun PlayerStateData.toDomain(): PlayerState = PlayerState(
    life = life,
    avatarStatus = AvatarStatus.valueOf(status),
    sitesControlled = sitesControlled,
    manaAvailable = manaAvailable,
    affinity = Element.entries.associateWith { element -> affinity[element.name] ?: 0 },
    avatarName = avatarName,
    avatarImageUri = avatarImageUri,
    pseudo = pseudo,
)
