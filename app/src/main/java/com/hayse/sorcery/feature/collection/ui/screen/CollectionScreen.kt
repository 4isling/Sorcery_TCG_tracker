package com.hayse.sorcery.feature.collection.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.LocalWindowWidthSizeClass
import com.hayse.sorcery.core.ui.ProvideCardList
import com.hayse.sorcery.core.ui.ProvideTopBarSearch
import com.hayse.sorcery.core.ui.composable.CardGridItem
import com.hayse.sorcery.core.ui.composable.CountBadge
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.isExpanded
import com.hayse.sorcery.core.ui.theme.LocalSetSkin
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.skin.Skins
import com.hayse.sorcery.core.ui.theme.skin.skinFor
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard
import com.hayse.sorcery.feature.collection.ui.screen.composable.AdvancedFilters
import com.hayse.sorcery.feature.collection.ui.screen.composable.CollectionQuantityStepper
import com.hayse.sorcery.feature.collection.ui.screen.composable.ImportModeDialog
import com.hayse.sorcery.feature.collection.ui.screen.composable.ImportReportDialog
import com.hayse.sorcery.feature.collection.ui.screen.composable.QuickFilterChips
import com.hayse.sorcery.feature.collection.ui.screen.composable.SetCompletionRow
import com.hayse.sorcery.feature.collection.ui.viewmodel.CollectionViewModel
import com.hayse.sorcery.feature.collection.ui.viewmodel.state.CollectionTab
import com.hayse.sorcery.feature.collection.ui.viewmodel.state.CollectionViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

private val CardGridColumns = GridCells.Adaptive(minSize = 120.dp)

@Composable
fun CollectionScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    fun runImport(uri: Uri, replace: Boolean) {
        scope.launch {
            val csv = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }
            if (csv != null) viewModel.import(csv, replace)
        }
    }

    val picker = rememberLauncherForActivityResult(
        // Le type MIME d'un CSV Curiosa varie selon l'appareil : on ouvre large.
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) pendingUri = uri }

    if (state.tab == CollectionTab.Collection) {
        ProvideTopBarSearch(
            query = state.filter.query.orEmpty(),
            onQueryChange = viewModel::setQuery,
            placeholder = stringResource(R.string.collection_search_placeholder),
        )
        ProvideCardList(state.items.map { it.card.name })
    }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = state.tab.ordinal) {
            CollectionTab.entries.forEach { tab ->
                Tab(
                    selected = state.tab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tabLabel(tab)) },
                )
            }
        }

        when (state.tab) {
            CollectionTab.Collection -> CollectionTabContent(
                state = state,
                viewModel = viewModel,
                onCardClick = onCardClick,
                onImport = { picker.launch(arrayOf("*/*")) },
            )
            CollectionTab.Completion -> CompletionList(
                completions = state.completions,
                missing = state.missing,
                expandedSet = state.selectedMissingSet,
                onToggle = { set ->
                    if (state.selectedMissingSet == set) viewModel.closeMissing()
                    else viewModel.openMissing(set)
                },
                onCardClick = onCardClick,
            )
            CollectionTab.Surplus -> SurplusList(state.surplus, onCardClick)
        }
    }

    pendingUri?.let { uri ->
        ImportModeDialog(
            onReplace = { pendingUri = null; runImport(uri, replace = true) },
            onMerge = { pendingUri = null; runImport(uri, replace = false) },
            onDismiss = { pendingUri = null },
        )
    }

    state.report?.let { ImportReportDialog(it, onDismiss = viewModel::dismissReport) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionTabContent(
    state: CollectionViewState,
    viewModel: CollectionViewModel,
    onCardClick: (String) -> Unit,
    onImport: () -> Unit,
) {
    if (LocalWindowWidthSizeClass.current.isExpanded) {
        ExpandedCollectionTabContent(state, viewModel, onCardClick, onImport)
        return
    }
    val gridState = rememberLazyGridState()
    // En-tête = bouton import (0) + filtres (1) : au-delà de l'index 1, les filtres sont sortis de l'écran.
    val filtersHidden by remember { derivedStateOf { gridState.firstVisibleItemIndex > 1 } }
    var showFilterSheet by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = CardGridColumns,
            contentPadding = PaddingValues(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "__import__", span = { GridItemSpan(maxLineSpan) }) {
                CollectionImportButton(importing = state.importing, onClick = onImport)
            }
            item(key = "__filters__", span = { GridItemSpan(maxLineSpan) }) { CollectionFilters(state, viewModel) }

            if (state.items.isEmpty()) {
                item(key = "__empty__", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.xl),
                        contentAlignment = Alignment.Center,
                    ) { Text(stringResource(R.string.collection_no_cards), style = MaterialTheme.typography.bodyLarge) }
                }
            } else {
                items(state.items, key = { it.card.name }) { item ->
                    CardGridItem(
                        imageUri = item.card.imageUri,
                        name = item.card.name,
                        onClick = { onCardClick(item.card.name) },
                        footer = { CollectionQuantityStepper(item, viewModel::adjust) },
                    )
                }
            }
        }

        if (filtersHidden) {
            SmallFloatingActionButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(spacing.md),
            ) { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.collection_filters)) }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            CollectionFilters(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = spacing.md).padding(bottom = spacing.lg),
            )
        }
    }
}

/** Vue tablette : filtres fixes dans un panneau latéral, grille des cartes à droite. */
@Composable
private fun ExpandedCollectionTabContent(
    state: CollectionViewState,
    viewModel: CollectionViewModel,
    onCardClick: (String) -> Unit,
    onImport: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            CollectionImportButton(importing = state.importing, onClick = onImport)
            CollectionFilters(state, viewModel)
        }
        VerticalDivider()
        if (state.items.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.collection_no_cards), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = CardGridColumns,
                contentPadding = PaddingValues(spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                items(state.items, key = { it.card.name }) { item ->
                    CardGridItem(
                        imageUri = item.card.imageUri,
                        name = item.card.name,
                        onClick = { onCardClick(item.card.name) },
                        footer = { CollectionQuantityStepper(item, viewModel::adjust) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionFilters(
    state: CollectionViewState,
    viewModel: CollectionViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.sm),
    ) {
        QuickFilterChips(
            ownership = state.filter.ownership,
            element = state.filter.element,
            onOwnership = viewModel::setOwnership,
            onElement = viewModel::setElement,
        )
        AdvancedFilters(
            rarity = state.filter.rarity,
            type = state.filter.type,
            setName = state.filter.setName,
            types = state.availableTypes,
            sets = state.availableSets,
            onRarity = viewModel::setRarity,
            onType = viewModel::setType,
            onSet = viewModel::setSet,
        )
    }
}

@Composable
private fun CollectionImportButton(
    importing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !importing,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            if (importing) stringResource(R.string.collection_importing)
            else stringResource(R.string.collection_import_csv),
        )
    }
}

@Composable
private fun CompletionList(
    completions: List<SetCompletion>,
    missing: List<Card>,
    expandedSet: String?,
    onToggle: (String) -> Unit,
    onCardClick: (String) -> Unit,
) {
    if (completions.isEmpty()) {
        EmptyState(title = stringResource(R.string.collection_no_set))
        return
    }
    val spacing = LocalSpacing.current
    // Décoration désactivée si le skin ambiant est neutre (mode Off).
    val skinEnabled = LocalSetSkin.current != Skins.Default
    LazyVerticalGrid(
        columns = CardGridColumns,
        contentPadding = PaddingValues(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        completions.forEach { completion ->
            item(key = completion.setName, span = { GridItemSpan(maxLineSpan) }) {
                val set = sorcerySetFromName(completion.setName)
                SorceryTheme(set = set, skin = if (skinEnabled) skinFor(set) else Skins.Default) {
                    SetCompletionRow(
                        completion = completion,
                        expanded = expandedSet == completion.setName,
                        onClick = { onToggle(completion.setName) },
                    )
                }
            }
            if (expandedSet == completion.setName) {
                if (missing.isEmpty()) {
                    item(key = "__complete_${completion.setName}", span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(spacing.md),
                            contentAlignment = Alignment.Center,
                        ) { Text(stringResource(R.string.collection_set_completed)) }
                    }
                } else {
                    items(missing, key = { "missing_${it.name}" }) { card ->
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
}

@Composable
private fun SurplusList(
    surplus: List<SurplusCard>,
    onCardClick: (String) -> Unit,
) {
    if (surplus.isEmpty()) {
        EmptyState(title = stringResource(R.string.collection_no_surplus))
        return
    }
    val spacing = LocalSpacing.current
    LazyVerticalGrid(
        columns = CardGridColumns,
        contentPadding = PaddingValues(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(surplus, key = { it.card.name }) { row ->
            CardGridItem(
                imageUri = row.card.imageUri,
                name = row.card.name,
                onClick = { onCardClick(row.card.name) },
                badge = { CountBadge("+${row.surplus}") },
            )
        }
    }
}

@Composable
private fun tabLabel(tab: CollectionTab): String = when (tab) {
    CollectionTab.Collection -> stringResource(R.string.collection_tab_collection)
    CollectionTab.Completion -> stringResource(R.string.collection_tab_completion)
    CollectionTab.Surplus -> stringResource(R.string.collection_tab_surplus)
}
