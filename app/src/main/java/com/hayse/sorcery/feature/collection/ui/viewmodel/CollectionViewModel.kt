package com.hayse.sorcery.feature.collection.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.buildGridRows
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.usecase.AdjustQuantityUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ImportCuriosaUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveCollectionUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveMissingUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveSetCompletionUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.ObserveSurplusUseCase
import com.hayse.sorcery.feature.collection.domain.usecase.SetQuantityUseCase
import com.hayse.sorcery.feature.collection.ui.viewmodel.state.CollectionTab
import com.hayse.sorcery.feature.collection.ui.viewmodel.state.CollectionViewState
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
class CollectionViewModel(
    private val cardRepository: CardRepository,
    private val observeCollection: ObserveCollectionUseCase,
    private val observeCompletion: ObserveSetCompletionUseCase,
    private val observeSurplus: ObserveSurplusUseCase,
    private val observeMissing: ObserveMissingUseCase,
    private val setQuantityUseCase: SetQuantityUseCase,
    private val adjustQuantity: AdjustQuantityUseCase,
    private val importCuriosa: ImportCuriosaUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(CollectionFilter())
    private val _selectedSet = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(CollectionViewState())
    val state: StateFlow<CollectionViewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    availableTypes = cardRepository.availableTypes(),
                    availableSets = cardRepository.availableSets(),
                )
            }
        }
        _filter
            .flatMapLatest { filter -> observeCollection(filter).map { filter to it } }
            .onEach { (filter, entries) -> _state.update { it.copy(filter = filter, rows = buildGridRows(entries)) } }
            .launchIn(viewModelScope)
        observeCompletion()
            .onEach { completions -> _state.update { it.copy(completions = completions) } }
            .launchIn(viewModelScope)
        observeSurplus()
            .onEach { surplus -> _state.update { it.copy(surplus = surplus) } }
            .launchIn(viewModelScope)
        _selectedSet
            .flatMapLatest { set -> if (set == null) flowOf(emptyList()) else observeMissing(set) }
            .onEach { missing -> _state.update { it.copy(missing = missing) } }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: CollectionTab) = _state.update { it.copy(tab = tab) }

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setOwnership(ownership: Ownership) = _filter.update { it.copy(ownership = ownership) }
    fun toggleElement(group: ElementGroup) = _filter.update { it.copy(element = it.element.toggled(group)) }
    fun setType(type: String?) = _filter.update { it.copy(type = type) }
    fun setRarity(rarity: Rarity?) = _filter.update { it.copy(rarity = rarity) }
    fun setSet(setName: String?) = _filter.update { it.copy(setName = setName) }

    fun openMissing(setName: String) {
        _selectedSet.value = setName
        _state.update { it.copy(selectedMissingSet = setName) }
    }

    fun closeMissing() {
        _selectedSet.value = null
        _state.update { it.copy(selectedMissingSet = null, missing = emptyList()) }
    }

    fun adjust(slug: String, finish: String, delta: Int) =
        viewModelScope.launch { adjustQuantity(slug, finish, delta) }

    fun setQuantity(slug: String, finish: String, quantity: Int) =
        viewModelScope.launch { setQuantityUseCase(slug, finish, quantity) }

    fun import(csv: String, replace: Boolean) = viewModelScope.launch {
        _state.update { it.copy(importing = true) }
        val report = importCuriosa(csv, replace)
        _state.update { it.copy(importing = false, report = report) }
    }

    fun dismissReport() = _state.update { it.copy(report = null) }
}
