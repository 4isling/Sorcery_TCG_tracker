package com.hayse.sorcery.core.shared.model

/**
 * Regroupement d'une carte selon ses éléments, pour l'affichage et le filtrage.
 * `Neutral` = aucun élément, `Multi` = plusieurs éléments. L'ordre de déclaration
 * est l'ordre de rangement voulu (Neutre, Earth, Fire, Water, Air, Multi).
 */
enum class ElementGroup {
    Neutral,
    Earth,
    Fire,
    Water,
    Air,
    Multi,
}

fun elementGroupOf(elements: List<Element>): ElementGroup = when {
    elements.isEmpty() -> ElementGroup.Neutral
    elements.size > 1 -> ElementGroup.Multi
    else -> when (elements.first()) {
        Element.Earth -> ElementGroup.Earth
        Element.Fire -> ElementGroup.Fire
        Element.Water -> ElementGroup.Water
        Element.Air -> ElementGroup.Air
    }
}
