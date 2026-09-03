package com.hayse.sorcery.feature.social.domain.model

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.Printing

/** Impression proposée à l'échange (miroir persistance). */
data class TradeableCopy(
    val slug: String,
    val finish: String,
    val quantity: Int,
)

/** Impression recherchée (miroir persistance). */
data class WantedCopy(
    val slug: String,
    val finish: String,
    val quantity: Int,
)

/** Ligne « À échanger » enrichie pour l'UI : carte + impression + quantités. */
data class TradeableItem(
    val card: Card,
    val printing: Printing,
    val ownedQty: Int,
    val tradeableQty: Int,
)

/** Ligne « Recherché » enrichie pour l'UI. */
data class WantedItem(
    val card: Card,
    val printing: Printing,
    val wantedQty: Int,
)
