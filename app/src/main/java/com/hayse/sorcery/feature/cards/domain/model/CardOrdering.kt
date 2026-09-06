package com.hayse.sorcery.feature.cards.domain.model

import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.shared.model.elementGroupOf

/** Une carte à placer sous un set donné, avec une charge utile (la carte elle-même côté
 *  explorateur, un [com.hayse.sorcery.feature.collection.domain.model.CollectionItem] côté collection). */
data class SetEntry<T>(val setName: String, val card: Card, val payload: T)

/** Ligne aplatie de la grille : en-tête de set, séparateur de groupe, ou cellule carte. */
sealed interface GridRow<out T> {
    data class SetHeader(val setName: String) : GridRow<Nothing>
    data class GroupLabel(val text: String) : GridRow<Nothing>
    data class Cell<T>(val setName: String, val card: Card, val payload: T) : GridRow<T>
}

const val AVATAR_TYPE = "Avatar"

private val TYPE_ORDER = listOf("Site", "Magic", "Aura", "Artifact", "Minion")

private val SET_ORDER = listOf("alpha", "beta", "arthurian legends", "gothic", "dragonlord", "promotional")

fun setRank(setName: String): Int =
    SET_ORDER.indexOf(setName.trim().lowercase()).let { if (it >= 0) it else SET_ORDER.size }

fun typeRank(type: String): Int =
    TYPE_ORDER.indexOfFirst { it.equals(type, ignoreCase = true) }.let { if (it >= 0) it else TYPE_ORDER.size }

fun rarityRank(rarity: Rarity?): Int = when (rarity) {
    Rarity.Unique -> 0
    Rarity.Elite -> 1
    Rarity.Exceptional -> 2
    Rarity.Ordinary -> 3
    null -> 4
}

private fun isAvatar(card: Card): Boolean = card.type.equals(AVATAR_TYPE, ignoreCase = true)

/** Ordre de tri des types de cartes disponibles pour les puces de filtre. */
fun sortTypesForFilter(types: List<String>): List<String> =
    types.sortedWith(compareBy({ typeRank(it) }, { it.lowercase() }))

private fun groupLabel(group: ElementGroup, type: String): String = "${group.name} · $type"

/**
 * Trie les entrées (set > Avatar > élément > type > rareté > nom) puis insère les en-têtes
 * de set et un séparateur à chaque changement de groupe d'élément ou de type.
 */
fun <T> buildGridRows(entries: List<SetEntry<T>>): List<GridRow<T>> {
    val sorted = entries.sortedWith(
        compareBy(
            { setRank(it.setName) },
            { it.setName.lowercase() },
            { if (isAvatar(it.card)) 0 else 1 },
            { elementGroupOf(it.card.elements).ordinal },
            { typeRank(it.card.type) },
            { rarityRank(it.card.rarity) },
            { it.card.name.lowercase() },
        ),
    )
    val rows = mutableListOf<GridRow<T>>()
    var curSet: String? = null
    var curElement: ElementGroup? = null
    var curType: String? = null
    for (e in sorted) {
        if (e.setName != curSet) {
            rows += GridRow.SetHeader(e.setName)
            curSet = e.setName
            curElement = null
            curType = null
        }
        val group = elementGroupOf(e.card.elements)
        if (group != curElement || !e.card.type.equals(curType, ignoreCase = true)) {
            rows += GridRow.GroupLabel(groupLabel(group, e.card.type))
            curElement = group
            curType = e.card.type
        }
        rows += GridRow.Cell(e.setName, e.card, e.payload)
    }
    return rows
}
