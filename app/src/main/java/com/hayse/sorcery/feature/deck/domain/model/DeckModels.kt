package com.hayse.sorcery.feature.deck.domain.model

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card

/** Les trois zones d'un deck Sorcery. */
enum class DeckSection { Avatar, Spellbook, Atlas }

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

/** Classe une carte dans sa zone à partir de son type (`Avatar`, `Site`, ou un sort). */
fun deckSectionOf(type: String): DeckSection = when (type) {
    "Avatar" -> DeckSection.Avatar
    "Site" -> DeckSection.Atlas
    else -> DeckSection.Spellbook
}

/** Une carte présente dans un deck : la carte, le nombre de copies, et combien on en possède. */
data class DeckEntry(
    val card: Card,
    val quantity: Int,
    val owned: Int,
) {
    /** Copies présentes dans le deck mais non possédées (à acquérir). */
    val missing: Int get() = (quantity - owned).coerceAtLeast(0)
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
    val avatar: DeckEntry? get() = entries.firstOrNull { deckSectionOf(it.card.type) == DeckSection.Avatar }
    val spellbook: List<DeckEntry> get() = entries.filter { deckSectionOf(it.card.type) == DeckSection.Spellbook }
    val atlas: List<DeckEntry> get() = entries.filter { deckSectionOf(it.card.type) == DeckSection.Atlas }
    val spellbookCount: Int get() = spellbook.sumOf { it.quantity }
    val atlasCount: Int get() = atlas.sumOf { it.quantity }
    val missingCount: Int get() = entries.sumOf { it.missing }
}
