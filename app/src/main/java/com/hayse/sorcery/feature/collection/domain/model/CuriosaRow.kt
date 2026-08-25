package com.hayse.sorcery.feature.collection.domain.model

/** Une ligne d'export Curiosa (product déjà normalisé : espaces -> underscores). */
data class CuriosaRow(
    val cardName: String,
    val set: String,
    val finish: String,
    val product: String,
    val quantity: Int,
)
