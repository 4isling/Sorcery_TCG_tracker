package com.hayse.sorcery.feature.cards.domain.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Rarity

data class Card(
    val name: String,
    val type: String,
    val rarity: Rarity?,
    val rulesText: String,
    val cost: Int?,
    val attack: Int?,
    val defence: Int?,
    val life: Int?,
    val thresholds: Map<Element, Int>,
    val elements: List<Element>,
    val subTypes: List<String>,
    /** URI Coil de l'image représentative (première impression), ou null si aucune. */
    val imageUri: String?,
)
