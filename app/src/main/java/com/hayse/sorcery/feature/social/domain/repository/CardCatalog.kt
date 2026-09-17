package com.hayse.sorcery.feature.social.domain.repository

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine

/**
 * Index local du catalogue nécessaire au calcul des suggestions d'échange. Construit depuis la
 * base de cartes (identique sur les deux téléphones) : aucune info de rareté ne transite par le
 * réseau, tout est résolu localement à partir des clés `(slug, finish)` échangées.
 */
data class SuggestionCatalog(
    /** slug d'impression -> nom de la carte. */
    val printingToCard: Map<String, String>,
    /** nom de carte -> rareté (null si inconnue). */
    val rarityByCard: Map<String, Rarity?>,
    /** set -> noms de cartes distincts qui y ont une impression. */
    val setCardsBySet: Map<String, Set<String>>,
    /** nom de carte -> sets où elle a une impression. */
    val setsByCard: Map<String, Set<String>>,
    /** nom de carte -> slugs de ses impressions (pour choisir un tirage à proposer). */
    val printingsByCard: Map<String, List<String>>,
)

/**
 * Résout des lignes d'échange brutes (`slug` + `finish` + quantité) en cartes affichables. Sert à
 * matérialiser la collection du pair et les offres reçues, qui n'arrivent que sous forme de clés.
 */
interface CardCatalog {

    /** Résout chaque ligne en [TradeCardLine] ; les impressions inconnues localement sont ignorées. */
    suspend fun resolve(lines: List<MatchLine>): List<TradeCardLine>

    /** Construit l'index catalogue (rareté, sets, impressions) pour le calcul des suggestions. */
    suspend fun suggestionCatalog(): SuggestionCatalog
}
