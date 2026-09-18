package com.hayse.sorcery.feature.deck.ui.viewmodel.state

import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.model.DeckValidation

data class DeckListViewState(
    val decks: List<DeckSummary> = emptyList(),
    val loading: Boolean = true,
)

enum class DeckEditorTab { Deck, Add }

data class DeckEditorViewState(
    val deckId: Long,
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val name: String = "",
    val format: DeckFormat = DeckFormat.Constructed,
    val avatarSetName: String? = null,
    val avatar: DeckEntry? = null,
    val spellbook: List<DeckEntry> = emptyList(),
    val atlas: List<DeckEntry> = emptyList(),
    val spellbookCount: Int = 0,
    val atlasCount: Int = 0,
    val missingCount: Int = 0,
    val validation: DeckValidation = DeckValidation(emptyList()),
    val tab: DeckEditorTab = DeckEditorTab.Deck,
    val filter: DeckCatalogFilter = DeckCatalogFilter(),
    val availableTypes: List<String> = emptyList(),
    val availableSets: List<String> = emptyList(),
    val catalog: List<DeckEntry> = emptyList(),
)
