package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.TradePayload
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.TradeMatch

/**
 * Croise deux payloads pour produire un [TradeMatch] du point de vue local. Logique pure,
 * testable en JVM.
 *
 * Une correspondance existe quand la même impression `(slug, finish)` figure dans ce qu'un côté
 * propose et dans ce que l'autre recherche ; la quantité retenue est le minimum des deux (on ne
 * peut échanger que ce qui est à la fois disponible et souhaité).
 */
object MatchComputations {

    fun compute(mine: TradePayload, peer: TradePayload): TradeMatch = TradeMatch(
        peerPseudo = peer.pseudo,
        iCanGive = intersect(offered = mine.tradeable, sought = peer.wanted),
        iCanReceive = intersect(offered = peer.tradeable, sought = mine.wanted),
    )

    private fun intersect(offered: List<PayloadEntry>, sought: List<PayloadEntry>): List<MatchLine> {
        val soughtByKey = sought
            .filter { it.qty > 0 }
            .associate { (it.slug to it.finish) to it.qty }
        return offered.asSequence()
            .filter { it.qty > 0 }
            .mapNotNull { entry ->
                val wantedQty = soughtByKey[entry.slug to entry.finish] ?: return@mapNotNull null
                MatchLine(entry.slug, entry.finish, minOf(entry.qty, wantedQty))
            }
            .sortedWith(compareBy({ it.slug }, { it.finish }))
            .toList()
    }
}
