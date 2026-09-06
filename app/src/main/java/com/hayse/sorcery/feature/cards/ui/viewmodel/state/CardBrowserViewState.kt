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
) {
    val cardNames: List<String>
        get() = rows.filterIsInstance<GridRow.Cell<Card>>().map { it.card.name }.distinct()
}
