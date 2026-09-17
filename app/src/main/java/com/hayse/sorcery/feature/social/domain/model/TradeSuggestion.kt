package com.hayse.sorcery.feature.social.domain.model

/**
 * Raison pour laquelle une carte est suggérée à l'échange, par priorité décroissante :
 * - [Wanted]       : la carte figure sur la liste « recherché » du destinataire.
 * - [CompletesSet] : le destinataire ne l'a pas et a commencé (sans finir) un set qui la contient.
 * - [Missing]      : le destinataire ne possède simplement aucune copie de la carte.
 */
enum class SuggestionReason { Wanted, CompletesSet, Missing }

/** Une carte suggérée à l'échange : impression concrète du donneur + carte + raison. */
data class SuggestionLine(
    val slug: String,
    val finish: String,
    val quantity: Int,
    val cardName: String,
    val reason: SuggestionReason,
)

/**
 * Résultat du calcul de suggestions du point de vue local :
 * - [iCanGive]    : mon surplus / « à échanger » qui comble un besoin du pair.
 * - [iCanReceive] : le surplus / « à échanger » du pair qui comble un de mes besoins.
 */
data class SuggestionResult(
    val peerPseudo: String?,
    val iCanGive: List<SuggestionLine>,
    val iCanReceive: List<SuggestionLine>,
) {
    val hasAny: Boolean get() = iCanGive.isNotEmpty() || iCanReceive.isNotEmpty()
}
