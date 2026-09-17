package com.hayse.sorcery.feature.cards.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.cards.domain.usecase.GetCardDetailUseCase
import com.hayse.sorcery.feature.cards.ui.viewmodel.state.CardDetailViewState
import com.hayse.sorcery.feature.collection.domain.usecase.AdjustQuantityUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveOwnedForCardUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.SetQuantityUseCase
import com.hayse.sorcery.feature.social.domain.usecase.ObserveWantedUseCase
import com.hayse.sorcery.feature.social.domain.usecase.SetWantedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardDetailViewModel(
    private val name: String,
    private val getCardDetail: GetCardDetailUseCase,
    private val observeOwned: ObserveOwnedForCardUseCase,
    private val setQuantityUseCase: SetQuantityUseCase,
    private val adjustQuantityUseCase: AdjustQuantityUseCase,
    private val observeWanted: ObserveWantedUseCase,
    private val setWantedUseCase: SetWantedUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CardDetailViewState())
    val state: StateFlow<CardDetailViewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = false, detail = getCardDetail(name)) }
        }
        observeOwned(name)
            .onEach { copies ->
                _state.update { st ->
                    st.copy(owned = copies.associate { (it.slug to it.finish) to it.quantity })
                }
            }
            .launchIn(viewModelScope)
        observeWanted()
            .onEach { items ->
                _state.update { st ->
                    st.copy(
                        wanted = items
                            .filter { it.card.name == name }
                            .associate { (it.printing.slug to it.printing.finish) to it.wantedQty },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun adjust(slug: String, finish: String, delta: Int) =
        viewModelScope.launch { adjustQuantityUseCase(slug, finish, delta) }

    fun setQuantity(slug: String, finish: String, quantity: Int) =
        viewModelScope.launch { setQuantityUseCase(slug, finish, quantity) }

    fun setWanted(slug: String, finish: String, quantity: Int) =
        viewModelScope.launch { setWantedUseCase(slug, finish, quantity) }
}
