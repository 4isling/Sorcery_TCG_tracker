package com.hayse.sorcery.feature.statistics.domain.model

import com.hayse.sorcery.core.shared.model.ElementGroup

/**
 * Composition scellée d'un set : combien de cartes contient un booster et combien de
 * boosters composent un display. Certains sets ne sont pas vendus en booster mais en
 * boîte fixe (mini-set) : [cardsPerBox] est alors renseigné à la place.
 */
data class SealedConfig(
    val cardsPerBooster: Int? = null,
    val boostersPerDisplay: Int? = null,
    val cardsPerBox: Int? = null,
)

/**
 * Compositions scellées connues, indexées par nom de set (tel que stocké dans `printings.setName`).
 * Data-driven : pour couvrir un nouveau set, ajouter simplement une entrée ici. Les sets absents
 * (ex. « Gothic », « Promotional ») n'affichent pas d'équivalent scellé.
 */
val SEALED_CONFIGS: Map<String, SealedConfig> = mapOf(
    "Alpha" to SealedConfig(cardsPerBooster = 15, boostersPerDisplay = 39),
    "Beta" to SealedConfig(cardsPerBooster = 15, boostersPerDisplay = 36),
    "Arthurian Legends" to SealedConfig(cardsPerBooster = 15, boostersPerDisplay = 24),
    "Dragonlord" to SealedConfig(cardsPerBox = 14),
)

/** Nombre de cartes possédées pour un groupe d'élément donné. */
data class ElementCount(val group: ElementGroup, val count: Int)

/**
 * Statistiques d'un set : cartes possédées et équivalent scellé. [boosters]/[displays] valent
 * `null` si le set n'a pas de config booster ; [boxes] est renseigné pour les mini-sets en boîte.
 */
data class SetStat(
    val setName: String,
    val ownedCards: Int,
    val boosters: Double? = null,
    val displays: Double? = null,
    val boxes: Double? = null,
)

/** Agrégat complet des statistiques de la collection possédée. */
data class CollectionStats(
    val totalCards: Int = 0,
    val distinctCards: Int = 0,
    val byElement: List<ElementCount> = emptyList(),
    val bySet: List<SetStat> = emptyList(),
    val totalBoosters: Double = 0.0,
    val totalDisplays: Double = 0.0,
)
