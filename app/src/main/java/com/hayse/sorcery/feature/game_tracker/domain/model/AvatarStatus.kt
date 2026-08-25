package com.hayse.sorcery.feature.game_tracker.domain.model

enum class AvatarStatus {
    /** L'avatar a de la vie et joue normalement. */
    Active,

    /** Vie à 0 : ne peut plus gagner de vie, immunisé au life loss direct, tout dégât suivant est fatal. */
    DeathsDoor,

    /** L'avatar a subi un death blow : partie perdue pour ce joueur. */
    Defeated,
}
