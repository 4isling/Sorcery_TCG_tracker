package com.hayse.sorcery.feature.deck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckIssue
import com.hayse.sorcery.feature.deck.domain.model.DeckSection
import com.hayse.sorcery.feature.deck.domain.model.DeckValidator
import com.hayse.sorcery.feature.deck.domain.repository.DeckRepository
import com.hayse.sorcery.feature.deck.domain.usecase.AdjustDeckCardQuantityUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.ObserveDeckCatalogUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.ObserveDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.RenameDeckUseCase
import com.hayse.sorcery.feature.deck.domain.usecase.SetDeckCardQuantityUseCase
import com.hayse.sorcery.feature.deck.ui.viewmodel.state.DeckEditorTab
import com.hayse.sorcery.feature.deck.ui.viewmodel.state.DeckEditorViewState
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
class DeckEditorViewModel(
    private val deckId: Long,
    private val repository: DeckRepository,
    private val observeDeck: ObserveDeckUseCase,
    private val observeCatalog: ObserveDeckCatalogUseCase,
    private val renameDeck: RenameDeckUseCase,
    private val setCard: SetDeckCardQuantityUseCase,
    private val adjustCard: AdjustDeckCardQuantityUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(DeckCatalogFilter())
    private val _state = MutableStateFlow(DeckEditorViewState(deckId = deckId))
    val state: StateFlow<DeckEditorViewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    availableTypes = repository.availableTypes(),
                    availableSets = repository.availableSets(),
                )
            }
        }

        observeDeck(deckId)
            .onEach { detail ->
                if (detail == null) {
                    _state.update { it.copy(loading = false, notFound = true) }
                } else {
                    _state.update {
                        it.copy(
                            loading = false,
                            name = detail.name,
                            format = detail.format,
                            avatar = detail.avatar,
                            spellbook = detail.spellbook,
                            atlas = detail.atlas,
                            spellbookCount = detail.spellbookCount,
                            atlasCount = detail.atlasCount,
                            missingCount = detail.missingCount,
                            validation = DeckValidator.validate(detail.format, detail.entries),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        _filter
            .flatMapLatest { filter -> observeCatalog(deckId, filter).map { filter to it } }
            .onEach { (filter, catalog) -> _state.update { it.copy(filter = filter, catalog = catalog) } }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: DeckEditorTab) = _state.update { it.copy(tab = tab) }

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setElement(element: Element?) = _filter.update { it.copy(element = element) }
    fun setType(type: String?) = _filter.update { it.copy(type = type) }
    fun setRarity(rarity: Rarity?) = _filter.update { it.copy(rarity = rarity) }
    fun setSet(setName: String?) = _filter.update { it.copy(setName = setName) }
    fun setOwnership(ownership: Ownership) = _filter.update { it.copy(ownership = ownership) }
    fun clearSection() = _filter.update { it.copy(section = null) }

    /** Ouvre l'onglet Ajouter filtré sur la section dont on a cliqué l'en-tête. */
    fun fillSection(section: DeckSection) {
        _filter.update {
            it.copy(
                section = section,
                query = null,
                type = null,
            )
        }
        _state.update { it.copy(tab = DeckEditorTab.Add) }
    }

    /** Ouvre l'onglet Ajouter en ciblant la section (ou la carte) de la condition cliquée. */
    fun fixIssue(issue: DeckIssue) {
        if (!issue.isFixable) return
        _filter.update {
            it.copy(
                section = issue.section,
                query = issue.cardName,
                type = null,
            )
        }
        _state.update { it.copy(tab = DeckEditorTab.Add) }
    }

    fun rename(name: String) {
        _state.update { it.copy(name = name) }
        viewModelScope.launch { renameDeck(deckId, name.trim().ifBlank { "Deck sans nom" }) }
    }

    fun adjust(cardName: String, delta: Int) =
        viewModelScope.launch { adjustCard(deckId, cardName, delta) }

    fun setQuantity(cardName: String, quantity: Int) =
        viewModelScope.launch { setCard(deckId, cardName, quantity) }
}
