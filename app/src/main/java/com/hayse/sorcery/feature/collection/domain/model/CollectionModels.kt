package com.hayse.sorcery.feature.collection.domain.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card

/** Une copie possédée : impression (slug) + finish + quantité. */
data class OwnedCopy(
    val slug: String,
    val finish: String,
    val quantity: Int,
)

/** Ligne de l'écran Collection : une carte et ses copies possédées. */
data class CollectionItem(
    val card: Card,
    val copies: List<OwnedCopy>,
    val totalQuantity: Int,
)

/** Avancement de collection sur un set (cartes distinctes possédées / total). */
data class SetCompletion(
    val setName: String,
    val owned: Int,
    val total: Int,
)

/** Carte possédée au-delà du playset autorisé par sa rareté. */
data class SurplusCard(
    val card: Card,
    val owned: Int,
    val maxCopies: Int,
    val surplus: Int,
)

/** Rapport d'import Curiosa. */
data class ImportReport(
    val matched: Int,
    val unmatched: List<CuriosaRow>,
    val ambiguous: List<CuriosaRow>,
    val invalid: List<String>,
)

/** Filtres de la collection ; un champ null = pas de contrainte. */
data class CollectionFilter(
    val query: String? = null,
    val element: Element? = null,
    val type: String? = null,
    val rarity: Rarity? = null,
    val setName: String? = null,
    val ownership: Ownership = Ownership.Owned,
)
