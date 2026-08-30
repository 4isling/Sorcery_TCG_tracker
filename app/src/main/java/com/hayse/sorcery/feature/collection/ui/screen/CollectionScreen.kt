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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.LocalWindowWidthSizeClass
import com.hayse.sorcery.core.ui.ProvideCardList
import com.hayse.sorcery.core.ui.ProvideTopBarSearch
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.isExpanded
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.collection.ui.screen.composable.AdvancedFilters
import com.hayse.sorcery.feature.collection.ui.screen.composable.CollectionCardRow
import com.hayse.sorcery.feature.collection.ui.screen.composable.ImportModeDialog
import com.hayse.sorcery.feature.collection.ui.screen.composable.ImportReportDialog
import com.hayse.sorcery.feature.collection.ui.screen.composable.MissingDialog
import com.hayse.sorcery.feature.collection.ui.screen.composable.QuickFilterChips
import com.hayse.sorcery.feature.collection.ui.screen.composable.SetCompletionRow
import com.hayse.sorcery.feature.collection.ui.screen.composable.SurplusRow
import com.hayse.sorcery.feature.collection.ui.viewmodel.CollectionViewModel
import com.hayse.sorcery.feature.collection.ui.viewmodel.state.CollectionTab
import com.hayse.sorcery.feature.collection.ui.viewmodel.state.CollectionViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun CollectionScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

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
            placeholder = "Rechercher une carte",
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

        OutlinedButton(
            onClick = { picker.launch(arrayOf("*/*")) },
            enabled = !state.importing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
        ) { Text(if (state.importing) "Import en cours…" else "Importer un CSV Curiosa") }

        when (state.tab) {
            CollectionTab.Collection -> CollectionTabContent(state, viewModel, onCardClick)
            CollectionTab.Completion -> CompletionList(state.completions, viewModel::openMissing)
            CollectionTab.Surplus -> SurplusList(state.surplus)
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

    state.selectedMissingSet?.let { set ->
        MissingDialog(setName = set, missing = state.missing, onDismiss = viewModel::closeMissing)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionTabContent(
    state: CollectionViewState,
    viewModel: CollectionViewModel,
    onCardClick: (String) -> Unit,
) {
    if (LocalWindowWidthSizeClass.current.isExpanded) {
        ExpandedCollectionTabContent(state, viewModel, onCardClick)
        return
    }
    val listState = rememberLazyListState()
    // Les filtres sont le 1er item : dès qu'on scrolle au-delà, ils sortent de l'écran.
    val filtersHidden by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    var showFilterSheet by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "__filters__") { CollectionFilters(state, viewModel) }

            if (state.items.isEmpty()) {
                item(key = "__empty__") {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(spacing.xl),
                        contentAlignment = Alignment.Center,
                    ) { Text("Aucune carte", style = MaterialTheme.typography.bodyLarge) }
                }
            } else {
                items(state.items, key = { it.card.name }) { item ->
                    CollectionCardRow(
                        item = item,
                        onClick = { onCardClick(item.card.name) },
                        onAdjust = viewModel::adjust,
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
            ) { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Filtres") }
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

/** Vue tablette : filtres fixes dans un panneau latéral, liste des cartes à droite. */
@Composable
private fun ExpandedCollectionTabContent(
    state: CollectionViewState,
    viewModel: CollectionViewModel,
    onCardClick: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(spacing.md),
        ) {
            CollectionFilters(state, viewModel)
        }
        VerticalDivider()
        if (state.items.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text("Aucune carte", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                items(state.items, key = { it.card.name }) { item ->
                    CollectionCardRow(
                        item = item,
                        onClick = { onCardClick(item.card.name) },
                        onAdjust = viewModel::adjust,
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
private fun CompletionList(
    completions: List<com.hayse.sorcery.feature.collection.domain.model.SetCompletion>,
    onSetClick: (String) -> Unit,
) {
    if (completions.isEmpty()) {
        EmptyState(title = "Aucun set")
        return
    }
    val spacing = LocalSpacing.current
    LazyColumn(
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(completions, key = { it.setName }) { completion ->
            SorceryTheme(set = sorcerySetFromName(completion.setName)) {
                SetCompletionRow(completion = completion, onClick = { onSetClick(completion.setName) })
            }
        }
    }
}

@Composable
private fun SurplusList(
    surplus: List<com.hayse.sorcery.feature.collection.domain.model.SurplusCard>,
) {
    if (surplus.isEmpty()) {
        EmptyState(title = "Aucun surplus")
        return
    }
    val spacing = LocalSpacing.current
    LazyColumn(
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(surplus, key = { it.card.name }) { row -> SurplusRow(row) }
    }
}

private fun tabLabel(tab: CollectionTab): String = when (tab) {
    CollectionTab.Collection -> "Collection"
    CollectionTab.Completion -> "Complétion"
    CollectionTab.Surplus -> "Surplus"
}
