package com.hayse.sorcery.feature.cards.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.ui.viewmodel.CardBrowserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardListScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardBrowserViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.filter.query.orEmpty(),
            onValueChange = viewModel::setQuery,
            label = { Text("Rechercher") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )

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

        when {
            state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.cards.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Aucune carte", style = MaterialTheme.typography.bodyLarge)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.cards, key = { it.name }) { card ->
                    CardGridItem(card = card, onClick = { onCardClick(card.name) })
                }
            }
        }
    }
}

@Composable
private fun CardGridItem(card: Card, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = card.imageUri,
            contentDescription = card.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f),
        )
        Text(
            text = card.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun OwnershipChips(selected: Ownership, onSelect: (Ownership) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Masquer les filtres avancés" else "Filtres avancés")
        }
        if (!expanded) return@Column
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Rarity.entries.forEach { rarity ->
                FilterChip(
                    selected = selectedRarity == rarity,
                    onClick = { onRarity(if (selectedRarity == rarity) null else rarity) },
                    label = { Text(rarity.name) },
                )
            }
            DropdownFilter(
                label = selectedType ?: "Type",
                options = types,
                selected = selectedType,
                onSelect = onType,
            )
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
