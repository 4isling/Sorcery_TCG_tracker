package com.hayse.sorcery.feature.collection.ui.viewmodel.state

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.ImportReport
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard

enum class CollectionTab { Collection, Completion, Surplus }

data class CollectionViewState(
    val tab: CollectionTab = CollectionTab.Collection,
    val filter: CollectionFilter = CollectionFilter(),
    val items: List<CollectionItem> = emptyList(),
    val completions: List<SetCompletion> = emptyList(),
    val surplus: List<SurplusCard> = emptyList(),
    val availableTypes: List<String> = emptyList(),
    val availableSets: List<String> = emptyList(),
    /** Set sélectionné depuis la complétion pour voir ses cartes manquantes (null = dialog fermé). */
    val selectedMissingSet: String? = null,
    val missing: List<Card> = emptyList(),
    val importing: Boolean = false,
    val report: ImportReport? = null,
)
