package com.hayse.sorcery.feature.cards.ui.viewmodel.state

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.model.GridRow

data class CardBrowserViewState(
    val loading: Boolean = true,
    val rows: List<GridRow<Card>> = emptyList(),
    val filter: CardFilter = CardFilter(),
    val availableTypes: List<String> = emptyList(),
    val availableSets: List<String> = emptyList(),
    /** Noms des cartes ayant au moins une impression sur la liste « recherché ». */
    val wantedCardNames: Set<String> = emptySet(),
    /** Noms des cartes dont au moins une impression est possédée. */
    val ownedCardNames: Set<String> = emptySet(),
    /** Sélection multiple active (long-press) ; non vide = mode sélection. */
    val selectedCardNames: Set<String> = emptySet(),
) {
    val cardNames: List<String>
        get() = rows.filterIsInstance<GridRow.Cell<Card>>().map { it.card.name }.distinct()

    val selectionActive: Boolean get() = selectedCardNames.isNotEmpty()
}
