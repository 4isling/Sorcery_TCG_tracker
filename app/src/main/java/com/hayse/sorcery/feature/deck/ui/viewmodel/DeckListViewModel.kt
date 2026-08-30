package com.hayse.sorcery.feature.deck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.usecase.CreateDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.DeleteDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.ObserveDecksUseCase
import com.hayse.sorcery.feature.deck.ui.viewmodel.state.DeckListViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeckListViewModel(
    private val observeDecks: ObserveDecksUseCase,
    private val createDeck: CreateDeckUseCase,
    private val deleteDeck: DeleteDeckUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DeckListViewState())
    val state: StateFlow<DeckListViewState> = _state.asStateFlow()

    init {
        observeDecks()
            .onEach { decks -> _state.update { it.copy(decks = decks, loading = false) } }
            .launchIn(viewModelScope)
    }

    fun create(name: String, format: DeckFormat, onCreated: (Long) -> Unit) = viewModelScope.launch {
        val id = createDeck(name.trim().ifBlank { "Nouveau deck" }, format)
        onCreated(id)
    }

    fun delete(deckId: Long) = viewModelScope.launch { deleteDeck(deckId) }
}
