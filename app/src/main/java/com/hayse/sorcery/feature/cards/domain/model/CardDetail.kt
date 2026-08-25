package com.hayse.sorcery.feature.cards.domain.model

data class CardDetail(
    val card: Card,
    val printings: List<Printing>,
)
