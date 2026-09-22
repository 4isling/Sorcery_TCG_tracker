package com.hayse.sorcery.feature.deck.domain.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card

/**
 * Les zones d'un deck Sorcery : l'Avatar, le Grimoire (sorts) et l'Atlas (sites) forment le deck
 * principal ; la Collection est la réserve de 10 cartes (sorts ou sites) qui complète le deck en
 * tournoi. Une carte de la Collection est saisie explicitement dans cette zone : son type ne
 * suffit pas à la classer.
 */
enum class DeckSection { Avatar, Spellbook, Atlas, Collection }

/** Filtres du catalogue de l'onglet "Ajouter". [section] est piloté par un clic sur une condition. */
data class DeckCatalogFilter(
    val query: String? = null,
    val element: Element? = null,
    val type: String? = null,
    val rarity: Rarity? = null,
    val setName: String? = null,
    val ownership: Ownership = Ownership.All,
    val section: DeckSection? = null,
)

/** Classe une carte dans sa zone principale à partir de son type (`Avatar`, `Site`, ou un sort). */
fun deckSectionOf(type: String): DeckSection = when (type) {
    "Avatar" -> DeckSection.Avatar
    "Site" -> DeckSection.Atlas
    else -> DeckSection.Spellbook
}

/** Une carte peut aller dans la Collection si elle n'est ni un Avatar ni un jeton. */
fun isCollectionEligible(type: String): Boolean = type != "Avatar" && type != "Token"

/**
 * Une carte présente dans un deck, dans une [section] donnée : la carte, le nombre de copies dans
 * cette zone, et combien on en possède au total. Une carte présente à la fois dans le deck
 * principal et dans la Collection donne deux entrées.
 */
data class DeckEntry(
    val card: Card,
    val quantity: Int,
    val owned: Int,
    val section: DeckSection = deckSectionOf(card.type),
) {
    val inCollection: Boolean get() = section == DeckSection.Collection

    /** Copies présentes dans cette zone mais non possédées (à acquérir). */
    val missing: Int get() = (quantity - owned).coerceAtLeast(0)
}

/** Copies à acquérir pour un ensemble d'entrées, la possession étant partagée entre les zones d'une même carte. */
fun List<DeckEntry>.missingTotal(): Int =
    groupBy { it.card.name }.values.sumOf { lines ->
        (lines.sumOf { it.quantity } - lines.first().owned).coerceAtLeast(0)
    }

/** Ligne de la liste des decks : résumé sans le détail des cartes. */
data class DeckSummary(
    val id: Long,
    val name: String,
    val format: DeckFormat,
    val avatarName: String?,
    val avatarSetName: String?,
    val avatarImageUri: String?,
    val spellbookCount: Int,
    val atlasCount: Int,
    val collectionCount: Int,
    val isLegal: Boolean,
    val updatedAt: Long,
)

/** Contenu complet d'un deck avec la possession résolue pour chaque carte. */
data class DeckDetail(
    val id: Long,
    val name: String,
    val format: DeckFormat,
    val entries: List<DeckEntry>,
    /** Set de l'avatar : donne son identité visuelle au deck (null si pas d'avatar). */
    val avatarSetName: String? = null,
) {
    val avatar: DeckEntry? get() = entries.firstOrNull { it.section == DeckSection.Avatar }
    val spellbook: List<DeckEntry> get() = entries.filter { it.section == DeckSection.Spellbook }
    val atlas: List<DeckEntry> get() = entries.filter { it.section == DeckSection.Atlas }
    val collection: List<DeckEntry> get() = entries.filter { it.section == DeckSection.Collection }
    val spellbookCount: Int get() = spellbook.sumOf { it.quantity }
    val atlasCount: Int get() = atlas.sumOf { it.quantity }
    val collectionCount: Int get() = collection.sumOf { it.quantity }
    val missingCount: Int get() = entries.missingTotal()
}
