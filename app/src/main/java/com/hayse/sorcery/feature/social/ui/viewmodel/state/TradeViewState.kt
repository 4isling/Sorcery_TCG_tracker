package com.hayse.sorcery.feature.social.ui.viewmodel.state

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.social.domain.model.TradeableItem
import com.hayse.sorcery.feature.social.domain.model.WantedItem

enum class TradeTab { Tradeable, Wanted }

data class TradeViewState(
    val tab: TradeTab = TradeTab.Tradeable,
    val tradeable: List<TradeableItem> = emptyList(),
    val wanted: List<WantedItem> = emptyList(),
    val showAddWanted: Boolean = false,
    val addWantedQuery: String = "",
    val addWantedResults: List<Card> = emptyList(),
    /** Carte choisie dans la recherche : on affiche alors ses impressions pour préciser slug+finish. */
    val addWantedSelection: CardDetail? = null,
)
