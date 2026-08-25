package com.hayse.sorcery.feature.cards.ui.viewmodel.state

import com.hayse.sorcery.feature.cards.domain.model.CardDetail

data class CardDetailViewState(
    val loading: Boolean = true,
    val detail: CardDetail? = null,
    /** Quantités possédées par (slug, finish) ; absent = 0. */
    val owned: Map<Pair<String, String>, Int> = emptyMap(),
)
