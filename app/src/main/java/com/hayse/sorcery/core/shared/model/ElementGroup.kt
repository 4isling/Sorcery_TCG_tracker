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

/** Sens d'un filtre par élément : ne garder que ce groupe, ou l'exclure. */
enum class ElementFilterMode { Include, Exclude }

/**
 * Sélection tri-état d'un filtre par groupe d'élément :
 * `group == null` = pas de filtre, `Include` = ce groupe uniquement, `Exclude` = tout sauf ce groupe.
 */
data class ElementSelection(
    val group: ElementGroup? = null,
    val mode: ElementFilterMode = ElementFilterMode.Include,
) {
    fun matches(elements: List<Element>): Boolean {
        val target = group ?: return true
        val isGroup = elementGroupOf(elements) == target
        return if (mode == ElementFilterMode.Include) isGroup else !isGroup
    }

    /** Cycle sur un chip : autre groupe -> Include ; même groupe : Include -> Exclude -> aucun. */
    fun toggled(clicked: ElementGroup): ElementSelection = when {
        group != clicked -> ElementSelection(clicked, ElementFilterMode.Include)
        mode == ElementFilterMode.Include -> ElementSelection(clicked, ElementFilterMode.Exclude)
        else -> ElementSelection()
    }
}
