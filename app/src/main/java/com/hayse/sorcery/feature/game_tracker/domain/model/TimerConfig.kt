package com.hayse.sorcery.feature.game_tracker.domain.model

/** Ce qui se produit quand le minuteur global de match arrive à zéro sans vainqueur. */
enum class TimerExpiryAction {
    /** Joue [TimerConfig.extraTurns] tours de mort subite, puis rend le verdict seuil-de-la-mort. */
    SuddenDeath,

    /** Verdict immédiat selon la règle du seuil de la mort, sans tours supplémentaires. */
    Verdict,

    /** Match nul immédiat. */
    Draw,

    /** Rien d'automatique : le temps est écoulé mais la partie continue. */
    None,
}

/**
 * Réglages du chrono d'une partie, choisis à la création. Le chrono est optionnel ([enabled]) et
 * chaque volet est indépendant : minuteur global de match ([useGlobal]) et/ou minuteur par tour
 * ([usePerTurn]). Quand le global tombe à zéro sans vainqueur, [expiry] décide de la suite (dont la
 * mort subite avec [extraTurns] tours).
 */
data class TimerConfig(
    val enabled: Boolean = false,
    val useGlobal: Boolean = true,
    val globalSeconds: Int = 50 * 60,
    val usePerTurn: Boolean = false,
    val perTurnSeconds: Int = 5 * 60,
    val extraTurns: Int = 3,
    val expiry: TimerExpiryAction = TimerExpiryAction.SuddenDeath,
) {
    companion object {
        val Disabled = TimerConfig(enabled = false)
    }
}
