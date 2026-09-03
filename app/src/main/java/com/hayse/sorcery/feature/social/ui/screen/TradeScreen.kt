package com.hayse.sorcery.feature.social.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.CardGridItem
import com.hayse.sorcery.core.ui.composable.CountBadge
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.social.ui.screen.composable.AddWantedSheetContent
import com.hayse.sorcery.feature.social.ui.screen.composable.TradeableFooter
import com.hayse.sorcery.feature.social.ui.screen.composable.WantedFooter
import com.hayse.sorcery.feature.social.ui.viewmodel.TradeViewModel
import com.hayse.sorcery.feature.social.ui.viewmodel.state.TradeTab
import com.hayse.sorcery.feature.social.ui.viewmodel.state.TradeViewState
import org.koin.androidx.compose.koinViewModel

private val CardGridColumns = GridCells.Adaptive(minSize = 120.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TradeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = state.tab.ordinal) {
            TradeTab.entries.forEach { tab ->
                Tab(
                    selected = state.tab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tabLabel(tab)) },
                )
            }
        }

        when (state.tab) {
            TradeTab.Tradeable -> TradeableGrid(state, viewModel, onCardClick)
            TradeTab.Wanted -> WantedGrid(state, viewModel, onCardClick)
        }
    }

    if (state.showAddWanted) {
        ModalBottomSheet(onDismissRequest = viewModel::closeAddWanted) {
            AddWantedSheetContent(
                query = state.addWantedQuery,
                results = state.addWantedResults,
                selection = state.addWantedSelection,
                onQueryChange = viewModel::onAddWantedQuery,
                onSelectCard = viewModel::selectCardForWanted,
                onClearSelection = viewModel::clearWantedSelection,
                onPickPrinting = viewModel::addWanted,
            )
        }
    }
}

@Composable
private fun TradeableGrid(
    state: TradeViewState,
    viewModel: TradeViewModel,
    onCardClick: (String) -> Unit,
) {
    if (state.tradeable.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.social_no_tradeable),
            subtitle = stringResource(R.string.social_no_tradeable_hint),
        )
        return
    }
    val spacing = LocalSpacing.current
    LazyVerticalGrid(
        columns = CardGridColumns,
        contentPadding = PaddingValues(spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.tradeable, key = { it.printing.slug }) { item ->
            CardGridItem(
                imageUri = item.printing.imageUri,
                name = item.card.name,
                onClick = { onCardClick(item.card.name) },
                badge = { if (item.tradeableQty > 0) CountBadge("↔${item.tradeableQty}") },
                footer = { TradeableFooter(item, viewModel::setTradeableQty) },
                modifier = Modifier.padding(spacing.xs),
            )
        }
    }
}

@Composable
private fun WantedGrid(
    state: TradeViewState,
    viewModel: TradeViewModel,
    onCardClick: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    Box(Modifier.fillMaxSize()) {
        if (state.wanted.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.social_no_wanted),
                subtitle = stringResource(R.string.social_no_wanted_hint),
            )
        } else {
            LazyVerticalGrid(
                columns = CardGridColumns,
                contentPadding = PaddingValues(spacing.md),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.wanted, key = { it.printing.slug }) { item ->
                    CardGridItem(
                        imageUri = item.printing.imageUri,
                        name = item.card.name,
                        onClick = { onCardClick(item.card.name) },
                        footer = { WantedFooter(item, viewModel::setWantedQty) },
                        modifier = Modifier.padding(spacing.xs),
                    )
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = viewModel::openAddWanted,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.social_add_wanted)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(spacing.md),
        )
    }
}

@Composable
private fun tabLabel(tab: TradeTab): String = when (tab) {
    TradeTab.Tradeable -> stringResource(R.string.social_tab_tradeable)
    TradeTab.Wanted -> stringResource(R.string.social_tab_wanted)
}
