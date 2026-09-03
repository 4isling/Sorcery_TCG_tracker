package com.hayse.sorcery.feature.social.domain.repository

import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine

/**
 * Résout des lignes d'échange brutes (`slug` + `finish` + quantité) en cartes affichables. Sert à
 * matérialiser la collection du pair et les offres reçues, qui n'arrivent que sous forme de clés.
 */
interface CardCatalog {

    /** Résout chaque ligne en [TradeCardLine] ; les impressions inconnues localement sont ignorées. */
    suspend fun resolve(lines: List<MatchLine>): List<TradeCardLine>
}
