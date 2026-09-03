package com.hayse.sorcery.feature.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.social.domain.model.SavedTradeItem
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedTradesViewModel(
    private val repository: SavedTradeRepository,
) : ViewModel() {

    val state: StateFlow<List<SavedTradeItem>> = repository.observeSavedTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun apply(id: Long) = viewModelScope.launch { repository.applyTrade(id) }
    fun delete(id: Long) = viewModelScope.launch { repository.deleteTrade(id) }
}
