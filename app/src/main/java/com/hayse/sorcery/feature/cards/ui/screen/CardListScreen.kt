package com.hayse.sorcery.feature.cards.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.ui.ProvideCardList
import com.hayse.sorcery.core.ui.ProvideTopBarSearch
import com.hayse.sorcery.core.ui.composable.CardGridItem
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.cards.ui.viewmodel.CardBrowserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardListScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardBrowserViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val set = sorcerySetFromName(state.filter.setName)

    ProvideTopBarSearch(
        query = state.filter.query.orEmpty(),
        onQueryChange = viewModel::setQuery,
        placeholder = "Rechercher une carte",
    )
    ProvideCardList(state.cards.map { it.name })

    SorceryTheme(set = set) {
        if (state.loading) {
            LoadingState(modifier.fillMaxSize())
            return@SorceryTheme
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = modifier.fillMaxSize(),
        ) {
            item(key = "__filters__", span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    OwnershipChips(
                        selected = state.filter.ownership,
                        onSelect = viewModel::setOwnership,
                    )
                    FilterBar(
                        selectedElement = state.filter.element,
                        selectedRarity = state.filter.rarity,
                        selectedType = state.filter.type,
                        selectedSet = state.filter.setName,
                        types = state.availableTypes,
                        sets = state.availableSets,
                        onElement = viewModel::setElement,
                        onRarity = viewModel::setRarity,
                        onType = viewModel::setType,
                        onSet = viewModel::setSet,
                    )
                }
            }
            if (state.cards.isEmpty()) {
                item(key = "__empty__", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(title = "Aucune carte")
                }
            } else {
                items(state.cards, key = { it.name }) { card ->
                    CardGridItem(
                        imageUri = card.imageUri,
                        name = card.name,
                        onClick = { onCardClick(card.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OwnershipChips(selected: Ownership, onSelect: (Ownership) -> Unit) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Ownership.entries.forEach { ownership ->
            FilterChip(
                selected = selected == ownership,
                onClick = { onSelect(ownership) },
                label = { Text(ownershipLabel(ownership)) },
            )
        }
    }
}

private fun ownershipLabel(ownership: Ownership): String = when (ownership) {
    Ownership.All -> "Toutes"
    Ownership.Owned -> "Possédées"
    Ownership.Missing -> "Manquantes"
    Ownership.Surplus -> "Surplus"
}

@Composable
private fun FilterBar(
    selectedElement: Element?,
    selectedRarity: Rarity?,
    selectedType: String?,
    selectedSet: String?,
    types: List<String>,
    sets: List<String>,
    onElement: (Element?) -> Unit,
    onRarity: (Rarity?) -> Unit,
    onType: (String?) -> Unit,
    onSet: (String?) -> Unit,
) {
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Masquer les filtres avancés" else "Filtres avancés")
        }
        if (!expanded) return@Column
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Element.entries.forEach { element ->
                FilterChip(
                    selected = selectedElement == element,
                    onClick = { onElement(if (selectedElement == element) null else element) },
                    label = { Text(element.name) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            types.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onType(if (selectedType == type) null else type) },
                    label = { Text(type) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Rarity.entries.forEach { rarity ->
                FilterChip(
                    selected = selectedRarity == rarity,
                    onClick = { onRarity(if (selectedRarity == rarity) null else rarity) },
                    label = { Text(rarity.name) },
                )
            }
            DropdownFilter(
                label = selectedSet ?: "Set",
                options = sets,
                selected = selectedSet,
                onSelect = onSet,
            )
        }
    }
}

@Composable
private fun DropdownFilter(
    label: String,
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Tous") },
                onClick = { onSelect(null); expanded = false },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}
