package com.hayse.sorcery.feature.deck.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.ProvideCardList
import com.hayse.sorcery.core.ui.ProvideTopBarSearch
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckSection
import com.hayse.sorcery.feature.deck.ui.screen.composable.DeckEntryRow
import com.hayse.sorcery.feature.deck.ui.screen.composable.DeckFilterBar
import com.hayse.sorcery.feature.deck.ui.screen.composable.SectionHeader
import com.hayse.sorcery.feature.deck.ui.screen.composable.ValidationSummary
import com.hayse.sorcery.feature.deck.ui.viewmodel.DeckEditorViewModel
import com.hayse.sorcery.feature.deck.ui.viewmodel.state.DeckEditorTab
import com.hayse.sorcery.feature.deck.ui.viewmodel.state.DeckEditorViewState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeckEditorScreen(
    deckId: Long,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeckEditorViewModel = koinViewModel(key = deckId.toString()) { parametersOf(deckId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    when {
        state.loading -> LoadingState(modifier)
        state.notFound -> EmptyState(title = "Deck introuvable", modifier = modifier)
        else -> Column(modifier = modifier.fillMaxSize()) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::rename,
                label = { Text("Nom du deck") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
            )

            SecondaryTabRow(selectedTabIndex = state.tab.ordinal) {
                DeckEditorTab.entries.forEach { tab ->
                    Tab(
                        selected = state.tab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tabLabel(tab)) },
                    )
                }
            }

            when (state.tab) {
                DeckEditorTab.Deck -> DeckContent(state, viewModel, onCardClick)
                DeckEditorTab.Add -> AddContent(state, viewModel, onCardClick)
            }
        }
    }
}

@Composable
private fun DeckContent(
    state: DeckEditorViewState,
    viewModel: DeckEditorViewModel,
    onCardClick: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    ProvideCardList(
        listOfNotNull(state.avatar?.card?.name) +
            state.spellbook.map { it.card.name } +
            state.atlas.map { it.card.name },
    )
    LazyColumn(
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "__validation__") {
            ValidationSummary(state.validation, onFixIssue = viewModel::fixIssue)
        }
        if (state.missingCount > 0) {
            item(key = "__missing__") {
                Text(
                    text = "${state.missingCount} carte(s) à acquérir pour compléter ce deck.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        item(key = "__avatar_header__") {
            SectionHeader(
                "Avatar",
                if (state.avatar != null) 1 else 0,
                minimum = 1,
                onClick = { viewModel.fillSection(DeckSection.Avatar) },
            )
        }
        state.avatar?.let { avatar ->
            item(key = "avatar_${avatar.card.name}") {
                DeckEntryRow(avatar, onClick = { onCardClick(avatar.card.name) }, onAdjust = viewModel::adjust)
            }
        }

        item(key = "__spellbook_header__") {
            SectionHeader(
                "Grimoire",
                state.spellbookCount,
                minimum = state.format.minSpellbook,
                onClick = { viewModel.fillSection(DeckSection.Spellbook) },
            )
        }
        deckEntries(state.spellbook, viewModel, onCardClick)

        item(key = "__atlas_header__") {
            SectionHeader(
                "Atlas",
                state.atlasCount,
                minimum = state.format.minAtlas,
                onClick = { viewModel.fillSection(DeckSection.Atlas) },
            )
        }
        deckEntries(state.atlas, viewModel, onCardClick)

        if (state.avatar == null && state.spellbook.isEmpty() && state.atlas.isEmpty()) {
            item(key = "__empty__") {
                Text(
                    text = "Deck vide. Ajoute des cartes depuis l'onglet Ajouter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddContent(
    state: DeckEditorViewState,
    viewModel: DeckEditorViewModel,
    onCardClick: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    ProvideTopBarSearch(
        query = state.filter.query.orEmpty(),
        onQueryChange = viewModel::setQuery,
        placeholder = "Rechercher une carte",
    )
    ProvideCardList(state.catalog.map { it.card.name })
    LazyColumn(
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "__filters__") {
            DeckFilterBar(
                filter = state.filter,
                types = state.availableTypes,
                sets = state.availableSets,
                onOwnership = viewModel::setOwnership,
                onElement = viewModel::setElement,
                onType = viewModel::setType,
                onRarity = viewModel::setRarity,
                onSet = viewModel::setSet,
                onClearSection = viewModel::clearSection,
            )
        }
        if (state.catalog.isEmpty()) {
            item(key = "__empty__") { EmptyState(title = "Aucune carte") }
        } else {
            items(state.catalog, key = { it.card.name }) { entry ->
                DeckEntryRow(entry, onClick = { onCardClick(entry.card.name) }, onAdjust = viewModel::adjust)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.deckEntries(
    entries: List<DeckEntry>,
    viewModel: DeckEditorViewModel,
    onCardClick: (String) -> Unit,
) {
    items(entries, key = { it.card.name }) { entry ->
        DeckEntryRow(entry, onClick = { onCardClick(entry.card.name) }, onAdjust = viewModel::adjust)
    }
}

private fun tabLabel(tab: DeckEditorTab): String = when (tab) {
    DeckEditorTab.Deck -> "Deck"
    DeckEditorTab.Add -> "Ajouter"
}
