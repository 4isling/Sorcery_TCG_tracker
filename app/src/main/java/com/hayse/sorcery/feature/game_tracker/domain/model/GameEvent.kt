package com.hayse.sorcery.feature.game_tracker.domain.model

import com.hayse.sorcery.core.shared.model.Element

/**
 * Événements de partie. L'historique de ces événements permet un undo trivial (pop + replay depuis
 * l'état initial). Chaque événement est déterministe une fois rejoué par [GameReducer].
 */
sealed interface GameEvent {
    /** Dégât à l'avatar : réduit la vie ; à death's door, tout dégât est un death blow (défaite). */
    data class Damage(val player: PlayerId, val amount: Int) : GameEvent

    /** Perte de vie directe : réduit la vie ; sans effet à death's door (immunité). */
    data class LifeLoss(val player: PlayerId, val amount: Int) : GameEvent

    /** Gain de vie : sans effet à death's door. */
    data class LifeGain(val player: PlayerId, val amount: Int) : GameEvent

    /** Ajustement direct du mana disponible (dépense pendant le tour). */
    data class ManaAdjust(val player: PlayerId, val delta: Int) : GameEvent

    /** Changement du nombre de sites contrôlés ; une prise de contrôle (delta > 0) ajoute du mana au pool courant. */
    data class SiteCountChange(val player: PlayerId, val delta: Int) : GameEvent

    /** Ajustement du compteur d'affinité élémentaire (seuil, pas une ressource dépensée). */
    data class AffinityChange(val player: PlayerId, val element: Element, val delta: Int) : GameEvent

    /** Nouveau tour : incrémente le compteur, bascule le joueur actif, reset son mana = nb de sites contrôlés. */
    data object NewTurn : GameEvent

    /**
     * Jet de dés. Les résultats sont tirés hors du reducer et stockés ici : le reducer est un no-op,
     * donc le replay ré-émet exactement les mêmes résultats (déterministe).
     */
    data class DiceRoll(
        val faces: Int,
        val results: List<Int>,
        val purpose: DiceRollPurpose = DiceRollPurpose.Generic,
    ) : GameEvent

    /** Tirage désignant le joueur qui commence ; [chosen] est stocké, jamais re-tiré au replay. */
    data class FirstPlayerRoll(val results: List<Int>, val chosen: PlayerId) : GameEvent
}

/** Contexte d'un [GameEvent.DiceRoll], pour l'affichage dans le journal. */
enum class DiceRollPurpose {
    /** Lanceur générique (nb dés × faces). */
    Generic,

    /** Setup du passif de l'avatar Harbinger : 3d20 pour choisir les cases. */
    Harbinger,
}
