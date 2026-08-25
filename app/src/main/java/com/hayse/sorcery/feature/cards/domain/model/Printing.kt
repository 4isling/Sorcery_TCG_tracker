package com.hayse.sorcery.feature.cards.domain.model

data class Printing(
    val slug: String,
    val setName: String,
    val releasedAt: String,
    val finish: String,
    val product: String,
    val artist: String,
    val flavorText: String,
    val typeText: String,
    val imageUri: String,
)
