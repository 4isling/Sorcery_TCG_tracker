package com.hayse.sorcery.feature.cards.data.local.model

import kotlinx.serialization.Serializable

/** Racine du fichier `assets/cards/cards.json` produit hors-app par tools/fetch_cards.py. */
@Serializable
data class CardCatalogJson(
    val cards: List<CardJson> = emptyList(),
)

@Serializable
data class CardJson(
    val name: String,
    val type: String,
    val rarity: String,
    val rulesText: String = "",
    val cost: Int? = null,
    val attack: Int? = null,
    val defence: Int? = null,
    val life: Int? = null,
    val thresholds: ThresholdsJson = ThresholdsJson(),
    val elements: String = "",
    val subTypes: String = "",
    val printings: List<PrintingJson> = emptyList(),
)

@Serializable
data class ThresholdsJson(
    val air: Int = 0,
    val earth: Int = 0,
    val fire: Int = 0,
    val water: Int = 0,
)

@Serializable
data class PrintingJson(
    val slug: String,
    val setName: String,
    val releasedAt: String = "",
    val finish: String = "",
    val product: String = "",
    val artist: String = "",
    val flavorText: String = "",
    val typeText: String = "",
)
