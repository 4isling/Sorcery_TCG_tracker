package com.hayse.sorcery.feature.game_tracker.domain.model

/**
 * Mode de suivi d'une partie. En [Solo], seul le joueur 1 est suivi (un panneau, pas de vis-à-vis) :
 * le joueur 2 existe dans l'état pour garder le reducer et le journal identiques, mais n'est pas
 * affiché. En [Duel], les deux joueurs sont suivis face à face.
 */
enum class GameMode { Solo, Duel }
