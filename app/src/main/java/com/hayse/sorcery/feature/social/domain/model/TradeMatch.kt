package com.hayse.sorcery.feature.social.domain.model

/** Une correspondance d'échange sur une impression précise. */
data class MatchLine(
    val slug: String,
    val finish: String,
    val quantity: Int,
)

/**
 * Résultat du croisement de deux listes d'échange (les miennes vs celles du pair), du point de
 * vue local :
 * - [iCanGive]   : ce que je propose et que le pair recherche.
 * - [iCanReceive]: ce que le pair propose et que je recherche.
 */
data class TradeMatch(
    val peerPseudo: String?,
    val iCanGive: List<MatchLine>,
    val iCanReceive: List<MatchLine>,
) {
    val hasAnyMatch: Boolean get() = iCanGive.isNotEmpty() || iCanReceive.isNotEmpty()
}
