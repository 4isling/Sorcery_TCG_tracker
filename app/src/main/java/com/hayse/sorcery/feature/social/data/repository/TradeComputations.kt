package com.hayse.sorcery.feature.social.data.repository

import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.social.domain.model.TradeableCopy
import com.hayse.sorcery.feature.social.domain.model.WantedCopy

/**
 * Logique pure des listes d'échange, sans dépendance à Room (testable en JVM).
 *
 * Une ligne « à échanger » n'existe que pour une impression réellement possédée, et sa quantité
 * proposée est plafonnée par la quantité possédée : si la collection diminue, le surplus stocké
 * est ignoré à l'affichage sans écriture destructive.
 */
object TradeComputations {

    data class TradeableRow(
        val slug: String,
        val finish: String,
        val ownedQty: Int,
        val tradeableQty: Int,
    )

    data class WantedRow(
        val slug: String,
        val finish: String,
        val wantedQty: Int,
    )

    fun tradeableRows(
        owned: List<OwnedCopy>,
        tradeable: List<TradeableCopy>,
    ): List<TradeableRow> {
        val storedByKey = tradeable.associateBy { it.slug to it.finish }
        return owned.asSequence()
            .filter { it.quantity > 0 }
            .map { copy ->
                val stored = storedByKey[copy.slug to copy.finish]?.quantity ?: 0
                TradeableRow(
                    slug = copy.slug,
                    finish = copy.finish,
                    ownedQty = copy.quantity,
                    tradeableQty = stored.coerceIn(0, copy.quantity),
                )
            }
            .sortedWith(compareBy({ it.slug }, { it.finish }))
            .toList()
    }

    fun wantedRows(wanted: List<WantedCopy>): List<WantedRow> =
        wanted.asSequence()
            .filter { it.quantity > 0 }
            .map { WantedRow(it.slug, it.finish, it.quantity) }
            .sortedWith(compareBy({ it.slug }, { it.finish }))
            .toList()
}
