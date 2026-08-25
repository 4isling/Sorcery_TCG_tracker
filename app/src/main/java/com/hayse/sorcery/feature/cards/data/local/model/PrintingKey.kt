package com.hayse.sorcery.feature.cards.data.local.model

/** Projection légère d'une impression, pour l'index de matching de l'import Curiosa. */
data class PrintingKey(
    val slug: String,
    val cardName: String,
    val setName: String,
    val finish: String,
    val product: String,
)
