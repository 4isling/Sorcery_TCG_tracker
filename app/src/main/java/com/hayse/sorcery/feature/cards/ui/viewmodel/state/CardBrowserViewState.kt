package com.hayse.sorcery.feature.cards.ui.viewmodel.state

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardFilter

data class CardBrowserViewState(
    val loading: Boolean = true,
    val cards: List<Card> = emptyList(),
    val filter: CardFilter = CardFilter(),
    val availableTypes: List<String> = emptyList(),
    val availableSets: List<String> = emptyList(),
)
