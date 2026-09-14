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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.ElementFilterMode
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.ElementSelection
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.ui.ProvideCardList
import com.hayse.sorcery.core.ui.ProvideTopBarSearch
import com.hayse.sorcery.core.ui.composable.CardGridItem
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.composable.GroupLabelRow
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.composable.SetHeaderRow
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.cards.domain.model.GridRow
import com.hayse.sorcery.feature.cards.domain.model.sortTypesForFilter
import com.hayse.sorcery.feature.cards.ui.viewmodel.CardBrowserViewModel
import org.koin.androidx.compose.koinViewModel

private val RARITY_FILTER_ORDER =
    listOf(Rarity.Unique, Rarity.Elite, Rarity.Exceptional, Rarity.Ordinary)

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
        placeholder = stringResource(R.string.cards_search_placeholder),
    )
    ProvideCardList(state.cardNames)

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
                        selectedSet = state.filter.setName,
                        sets = state.availableSets,
                        onSet = viewModel::setSet,
                    )
                    FilterBar(
                        element = state.filter.element,
                        selectedRarity = state.filter.rarity,
                        selectedType = state.filter.type,
                        types = state.availableTypes,
                        onElement = viewModel::toggleElement,
                        onRarity = viewModel::setRarity,
                        onType = viewModel::setType,
                    )
                }
            }
            if (state.rows.isEmpty()) {
                item(key = "__empty__", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(title = stringResource(R.string.cards_empty))
                }
            } else {
                state.rows.forEachIndexed { i, row ->
                    when (row) {
                        is GridRow.SetHeader -> item(
                            key = "set_$i",
                            span = { GridItemSpan(maxLineSpan) },
                        ) { SetHeaderRow(row.setName) }

                        is GridRow.GroupLabel -> item(
                            key = "grp_$i",
                            span = { GridItemSpan(maxLineSpan) },
                        ) { GroupLabelRow(row.text) }

                        is GridRow.Cell -> item(key = "${row.setName}_${row.card.name}") {
                            CardGridItem(
                                imageUri = row.card.imageUri,
                                name = row.card.name,
                                onClick = { onCardClick(row.card.name) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnershipChips(
    selected: Ownership,
    onSelect: (Ownership) -> Unit,
    selectedSet: String?,
    sets: List<String>,
    onSet: (String?) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ownership.entries.forEach { ownership ->
            FilterChip(
                selected = selected == ownership,
                onClick = { onSelect(ownership) },
                label = { Text(ownershipLabel(ownership)) },
            )
        }
        DropdownFilter(
            label = selectedSet ?: stringResource(R.string.cards_filter_set),
            options = sets,
            selected = selectedSet,
            onSelect = onSet,
        )
    }
}

@Composable
private fun ownershipLabel(ownership: Ownership): String = when (ownership) {
    Ownership.All -> stringResource(R.string.cards_ownership_all)
    Ownership.Owned -> stringResource(R.string.cards_ownership_owned)
    Ownership.Missing -> stringResource(R.string.cards_ownership_missing)
    Ownership.Surplus -> stringResource(R.string.cards_ownership_surplus)
}

@Composable
private fun FilterBar(
    element: ElementSelection,
    selectedRarity: Rarity?,
    selectedType: String?,
    types: List<String>,
    onElement: (ElementGroup) -> Unit,
    onRarity: (Rarity?) -> Unit,
    onType: (String?) -> Unit,
) {
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) stringResource(R.string.cards_hide_advanced_filters) else stringResource(R.string.cards_advanced_filters))
        }
        if (!expanded) return@Column
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            ElementGroup.entries.forEach { group ->
                val active = element.group == group
                FilterChip(
                    selected = active,
                    onClick = { onElement(group) },
                    leadingIcon = if (active && element.mode == ElementFilterMode.Exclude) {
                        {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else null,
                    label = { Text(group.name) },
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
            sortTypesForFilter(types).forEach { type ->
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
            RARITY_FILTER_ORDER.forEach { rarity ->
                FilterChip(
                    selected = selectedRarity == rarity,
                    onClick = { onRarity(if (selectedRarity == rarity) null else rarity) },
                    label = { Text(rarity.name) },
                )
            }
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
                text = { Text(stringResource(R.string.cards_filter_all)) },
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
