package com.hayse.sorcery.feature.cards.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.model.buildGridRows
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.cards.domain.usecase.GetCardDetailUseCase
import com.hayse.sorcery.feature.cards.domain.usecase.ObserveCardsUseCase
import com.hayse.sorcery.feature.cards.ui.viewmodel.state.CardBrowserViewState
import com.hayse.sorcery.feature.social.domain.model.WantedItem
import com.hayse.sorcery.feature.social.domain.usecase.ObserveWantedUseCase
import com.hayse.sorcery.feature.social.domain.usecase.SetWantedUseCase
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
    private val observeWanted: ObserveWantedUseCase,
    private val setWantedUseCase: SetWantedUseCase,
    private val getCardDetail: GetCardDetailUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(CardFilter())
    private val _state = MutableStateFlow(CardBrowserViewState())
    val state: StateFlow<CardBrowserViewState> = _state.asStateFlow()

    /** Impressions recherchées par nom de carte, pour pouvoir retirer une carte de la liste. */
    private var wantedByCard: Map<String, List<WantedItem>> = emptyMap()

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
        observeWanted()
            .onEach { items ->
                wantedByCard = items.groupBy { it.card.name }
                _state.update { it.copy(wantedCardNames = wantedByCard.keys) }
            }
            .launchIn(viewModelScope)
        repository.observeOwnedCardNames()
            .onEach { names -> _state.update { it.copy(ownedCardNames = names) } }
            .launchIn(viewModelScope)
    }

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun toggleElement(group: ElementGroup) = _filter.update { it.copy(element = it.element.toggled(group)) }
    fun setType(type: String?) = _filter.update { it.copy(type = type) }
    fun setRarity(rarity: Rarity?) = _filter.update { it.copy(rarity = rarity) }
    fun setSet(setName: String?) = _filter.update { it.copy(setName = setName) }
    fun setOwnership(ownership: Ownership) = _filter.update { it.copy(ownership = ownership) }

    /** Long-press : entre en mode sélection en marquant la carte. */
    fun startSelection(cardName: String) =
        _state.update { it.copy(selectedCardNames = it.selectedCardNames + cardName) }

    /** Tap en mode sélection : ajoute/retire la carte (vider la sélection = quitter le mode). */
    fun toggleSelected(cardName: String) = _state.update {
        val next = if (cardName in it.selectedCardNames) it.selectedCardNames - cardName
        else it.selectedCardNames + cardName
        it.copy(selectedCardNames = next)
    }

    fun clearSelection() = _state.update { it.copy(selectedCardNames = emptySet()) }

    /**
     * Ajoute les cartes sélectionnées à la liste « recherché » (tirage de base Booster/Standard,
     * sinon la 1re impression), puis quitte le mode sélection.
     */
    fun addSelectedToWanted() = viewModelScope.launch {
        _state.value.selectedCardNames.forEach { cardName ->
            if (cardName in wantedByCard) return@forEach
            val printings = getCardDetail(cardName)?.printings ?: return@forEach
            val base = printings.firstOrNull { it.product == "Booster" && it.finish == "Standard" }
                ?: printings.firstOrNull() ?: return@forEach
            setWantedUseCase(base.slug, base.finish, 1)
        }
        clearSelection()
    }
}
