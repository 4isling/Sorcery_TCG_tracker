package com.hayse.sorcery.feature.game_tracker.domain.model

/** Issue d'une partie. [winner]/[loser] à null = fin sans vaincu (nul ou abandon). */
data class GameOutcome(
    val winner: PlayerId?,
    val loser: PlayerId?,
    val turns: Int,
)

/**
 * Dérive l'issue de l'état courant : le perdant est le joueur vaincu (death blow), le vainqueur son
 * adversaire. Sans joueur vaincu, l'issue est un nul/abandon (vainqueur et perdant null).
 */
fun GameState.outcome(): GameOutcome {
    val loser = players.entries.firstOrNull { it.value.avatarStatus == AvatarStatus.Defeated }?.key
    return GameOutcome(winner = loser?.other(), loser = loser, turns = turn)
}
