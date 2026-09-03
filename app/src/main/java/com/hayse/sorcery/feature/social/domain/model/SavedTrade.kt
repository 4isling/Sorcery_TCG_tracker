package com.hayse.sorcery.feature.social.domain.model

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.Printing

/** Une ligne d'échange résolue pour l'affichage (impression + quantité). */
data class TradeCardLine(
    val card: Card,
    val printing: Printing,
    val quantity: Int,
)

/**
 * Échange planifié avec un pair, prêt à être affiché ou appliqué. [iGive]/[iReceive] sont résolus
 * en cartes ; [done] indique s'il a déjà été appliqué à la collection.
 */
data class SavedTradeItem(
    val id: Long,
    val peerPseudo: String,
    val createdAt: Long,
    val iGive: List<TradeCardLine>,
    val iReceive: List<TradeCardLine>,
    val done: Boolean,
)
