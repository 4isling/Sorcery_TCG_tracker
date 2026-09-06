package com.hayse.sorcery.feature.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.social.domain.usecase.ObserveTradeableUseCase
import com.hayse.sorcery.feature.social.domain.usecase.ObserveWantedUseCase
import com.hayse.sorcery.feature.social.domain.usecase.SetTradeableUseCase
import com.hayse.sorcery.feature.social.domain.usecase.SetWantedUseCase
import com.hayse.sorcery.feature.social.ui.viewmodel.state.TradeTab
import com.hayse.sorcery.feature.social.ui.viewmodel.state.TradeViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TradeViewModel(
    private val observeTradeable: ObserveTradeableUseCase,
    private val observeWanted: ObserveWantedUseCase,
    private val setTradeable: SetTradeableUseCase,
    private val setWanted: SetWantedUseCase,
    private val cardRepository: CardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TradeViewState())
    val state: StateFlow<TradeViewState> = _state.asStateFlow()

    private val _addQuery = MutableStateFlow("")

    init {
        observeTradeable()
            .onEach { list -> _state.update { it.copy(tradeable = list) } }
            .launchIn(viewModelScope)
        observeWanted()
            .onEach { list -> _state.update { it.copy(wanted = list) } }
            .launchIn(viewModelScope)
        _addQuery
            .flatMapLatest { query ->
                if (query.isBlank()) flowOf(emptyList())
                else cardRepository.observeCards(CardFilter(query = query))
                    .map { entries -> entries.map { it.card }.distinctBy { it.name } }
            }
            .onEach { results -> _state.update { it.copy(addWantedResults = results.take(MAX_RESULTS)) } }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: TradeTab) = _state.update { it.copy(tab = tab) }

    fun setTradeableQty(slug: String, finish: String, quantity: Int) =
        viewModelScope.launch { setTradeable(slug, finish, quantity) }

    fun setWantedQty(slug: String, finish: String, quantity: Int) =
        viewModelScope.launch { setWanted(slug, finish, quantity) }

    fun removeWanted(slug: String, finish: String) = setWantedQty(slug, finish, 0)

    fun openAddWanted() {
        _addQuery.value = ""
        _state.update {
            it.copy(
                showAddWanted = true,
                addWantedQuery = "",
                addWantedResults = emptyList(),
                addWantedSelection = null,
            )
        }
    }

    fun closeAddWanted() = _state.update { it.copy(showAddWanted = false, addWantedSelection = null) }

    fun onAddWantedQuery(query: String) {
        _state.update { it.copy(addWantedQuery = query, addWantedSelection = null) }
        _addQuery.value = query
    }

    fun selectCardForWanted(name: String) = viewModelScope.launch {
        val detail = cardRepository.getCard(name)
        _state.update { it.copy(addWantedSelection = detail) }
    }

    fun clearWantedSelection() = _state.update { it.copy(addWantedSelection = null) }

    fun addWanted(slug: String, finish: String) {
        viewModelScope.launch { setWanted(slug, finish, 1) }
        closeAddWanted()
    }

    private companion object {
        const val MAX_RESULTS = 30
    }
}
