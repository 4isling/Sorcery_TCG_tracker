package com.hayse.sorcery.feature.cards.domain.model

import com.hayse.sorcery.core.shared.model.ElementSelection
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity

/** Filtres du navigateur de cartes ; un champ null = pas de contrainte. */
data class CardFilter(
    val query: String? = null,
    val element: ElementSelection = ElementSelection(),
    val type: String? = null,
    val rarity: Rarity? = null,
    val setName: String? = null,
    val ownership: Ownership = Ownership.All,
)
