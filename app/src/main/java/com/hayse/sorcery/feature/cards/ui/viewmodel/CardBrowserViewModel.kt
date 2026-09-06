package com.hayse.sorcery.feature.cards.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.model.buildGridRows
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.cards.domain.usecase.ObserveCardsUseCase
import com.hayse.sorcery.feature.cards.ui.viewmodel.state.CardBrowserViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CardBrowserViewModel(
    private val repository: CardRepository,
    private val observeCards: ObserveCardsUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(CardFilter())
    private val _state = MutableStateFlow(CardBrowserViewState())
    val state: StateFlow<CardBrowserViewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            _state.update {
                it.copy(
                    availableTypes = repository.availableTypes(),
                    availableSets = repository.availableSets(),
                )
            }
        }
        _filter
            .flatMapLatest { filter -> observeCards(filter).map { entries -> filter to entries } }
            .onEach { (filter, entries) ->
                _state.update { it.copy(filter = filter, rows = buildGridRows(entries), loading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setElementGroup(group: ElementGroup?) = _filter.update { it.copy(elementGroup = group) }
    fun setType(type: String?) = _filter.update { it.copy(type = type) }
    fun setRarity(rarity: Rarity?) = _filter.update { it.copy(rarity = rarity) }
    fun setSet(setName: String?) = _filter.update { it.copy(setName = setName) }
    fun setOwnership(ownership: Ownership) = _filter.update { it.copy(ownership = ownership) }
}
