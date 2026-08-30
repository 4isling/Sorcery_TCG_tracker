package com.hayse.sorcery.feature.game_tracker.domain

import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus
import com.hayse.sorcery.feature.game_tracker.domain.model.GameEvent
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerState

/**
 * Réduit un [GameEvent] sur un [GameState] sans toucher à l'historique (pur, déterministe, rejouable).
 * Toutes les règles Sorcery du tracker vivent ici. Voir [record] pour la variante qui journalise l'événement.
 */
object GameReducer {

    fun apply(state: GameState, event: GameEvent): GameState = when (event) {
        is GameEvent.Damage -> {
            if (event.amount <= 0) state
            else state.withPlayer(event.player) { applyDamage(it, event.amount) }
        }

        is GameEvent.LifeLoss -> {
            if (event.amount <= 0) state
            else state.withPlayer(event.player) { applyLifeLoss(it, event.amount) }
        }

        is GameEvent.LifeGain -> {
            if (event.amount <= 0) state
            else state.withPlayer(event.player) { applyLifeGain(it, event.amount) }
        }

        is GameEvent.ManaAdjust -> state.withPlayer(event.player) {
            it.copy(manaAvailable = (it.manaAvailable + event.delta).coerceAtLeast(0))
        }

        is GameEvent.SiteCountChange -> state.withPlayer(event.player) {
            val newSites = (it.sitesControlled + event.delta).coerceAtLeast(0)
            // Une prise de contrôle en cours de tour ajoute immédiatement son mana au pool.
            val manaBonus = if (event.delta > 0) event.delta else 0
            it.copy(sitesControlled = newSites, manaAvailable = it.manaAvailable + manaBonus)
        }

        is GameEvent.AffinityChange -> state.withPlayer(event.player) {
            val current = it.affinity[event.element] ?: 0
            val updated = (current + event.delta).coerceAtLeast(0)
            it.copy(affinity = it.affinity + (event.element to updated))
        }

        GameEvent.NewTurn -> {
            val next = state.activePlayer.other()
            state
                .copy(turn = state.turn + 1, activePlayer = next)
                .withPlayer(next) { it.copy(manaAvailable = it.sitesControlled) }
        }

        // Les résultats sont déjà tirés et portés par l'événement : no-op déterministe au replay.
        is GameEvent.DiceRoll -> state

        is GameEvent.FirstPlayerRoll -> state.copy(activePlayer = event.chosen)
    }

    private fun applyDamage(player: PlayerState, amount: Int): PlayerState = when (player.avatarStatus) {
        AvatarStatus.Defeated -> player
        // Death blow : tout dégât à death's door est fatal.
        AvatarStatus.DeathsDoor -> player.copy(avatarStatus = AvatarStatus.Defeated)
        AvatarStatus.Active -> {
            val newLife = (player.life - amount).coerceAtLeast(0)
            if (newLife == 0) player.copy(life = 0, avatarStatus = AvatarStatus.DeathsDoor)
            else player.copy(life = newLife)
        }
    }

    private fun applyLifeLoss(player: PlayerState, amount: Int): PlayerState = when (player.avatarStatus) {
        // Immunité au life loss direct à death's door ; sans objet si déjà défait.
        AvatarStatus.DeathsDoor, AvatarStatus.Defeated -> player
        AvatarStatus.Active -> {
            val newLife = (player.life - amount).coerceAtLeast(0)
            if (newLife == 0) player.copy(life = 0, avatarStatus = AvatarStatus.DeathsDoor)
            else player.copy(life = newLife)
        }
    }

    private fun applyLifeGain(player: PlayerState, amount: Int): PlayerState = when (player.avatarStatus) {
        // Ne peut plus gagner de vie à death's door.
        AvatarStatus.DeathsDoor, AvatarStatus.Defeated -> player
        AvatarStatus.Active -> player.copy(life = player.life + amount)
    }
}

/** Applique [event] puis le journalise dans l'historique (undo par pop + replay). */
fun GameState.record(event: GameEvent): GameState =
    GameReducer.apply(this, event).copy(history = history + event)
