package com.hayse.sorcery.feature.game_tracker.data.local.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.DiceRollPurpose
import com.hayse.sorcery.feature.game_tracker.domain.model.GameEvent
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Représentation persistée d'un [GameEvent] (historique de la partie en cours). */
@Serializable
sealed interface GameEventData {

    @Serializable
    @SerialName("damage")
    data class Damage(val player: String, val amount: Int) : GameEventData

    @Serializable
    @SerialName("life_loss")
    data class LifeLoss(val player: String, val amount: Int) : GameEventData

    @Serializable
    @SerialName("life_gain")
    data class LifeGain(val player: String, val amount: Int) : GameEventData

    @Serializable
    @SerialName("mana_adjust")
    data class ManaAdjust(val player: String, val delta: Int) : GameEventData

    @Serializable
    @SerialName("site_count_change")
    data class SiteCountChange(val player: String, val delta: Int) : GameEventData

    @Serializable
    @SerialName("affinity_change")
    data class AffinityChange(val player: String, val element: String, val delta: Int) : GameEventData

    @Serializable
    @SerialName("new_turn")
    data object NewTurn : GameEventData

    @Serializable
    @SerialName("dice_roll")
    data class DiceRoll(val faces: Int, val results: List<Int>, val purpose: String) : GameEventData

    @Serializable
    @SerialName("first_player_roll")
    data class FirstPlayerRoll(val results: List<Int>, val chosen: String) : GameEventData
}

fun GameEvent.toData(): GameEventData = when (this) {
    is GameEvent.Damage -> GameEventData.Damage(player.name, amount)
    is GameEvent.LifeLoss -> GameEventData.LifeLoss(player.name, amount)
    is GameEvent.LifeGain -> GameEventData.LifeGain(player.name, amount)
    is GameEvent.ManaAdjust -> GameEventData.ManaAdjust(player.name, delta)
    is GameEvent.SiteCountChange -> GameEventData.SiteCountChange(player.name, delta)
    is GameEvent.AffinityChange -> GameEventData.AffinityChange(player.name, element.name, delta)
    GameEvent.NewTurn -> GameEventData.NewTurn
    is GameEvent.DiceRoll -> GameEventData.DiceRoll(faces, results, purpose.name)
    is GameEvent.FirstPlayerRoll -> GameEventData.FirstPlayerRoll(results, chosen.name)
}

fun GameEventData.toDomain(): GameEvent = when (this) {
    is GameEventData.Damage -> GameEvent.Damage(PlayerId.valueOf(player), amount)
    is GameEventData.LifeLoss -> GameEvent.LifeLoss(PlayerId.valueOf(player), amount)
    is GameEventData.LifeGain -> GameEvent.LifeGain(PlayerId.valueOf(player), amount)
    is GameEventData.ManaAdjust -> GameEvent.ManaAdjust(PlayerId.valueOf(player), delta)
    is GameEventData.SiteCountChange -> GameEvent.SiteCountChange(PlayerId.valueOf(player), delta)
    is GameEventData.AffinityChange ->
        GameEvent.AffinityChange(PlayerId.valueOf(player), Element.valueOf(element), delta)
    GameEventData.NewTurn -> GameEvent.NewTurn
    is GameEventData.DiceRoll -> GameEvent.DiceRoll(faces, results, DiceRollPurpose.valueOf(purpose))
    is GameEventData.FirstPlayerRoll -> GameEvent.FirstPlayerRoll(results, PlayerId.valueOf(chosen))
}
