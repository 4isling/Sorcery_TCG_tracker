package com.hayse.sorcery.feature.collection.ui.screen.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.ui.composable.AdjustButton
import com.hayse.sorcery.core.ui.theme.skin.SkinnedCard
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion

/** Ligne de complétion d'un set : nom + owned/total + barre. Cliquable pour déplier les manquants. */
@Composable
fun SetCompletionRow(completion: SetCompletion, expanded: Boolean, onClick: () -> Unit) {
    SkinnedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(completion.setName, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${completion.owned} / ${completion.total}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = stringResource(R.string.collection_view_missing),
                    )
                }
            }
            val fraction = if (completion.total > 0) completion.owned.toFloat() / completion.total else 0f
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Steppers compacts (un par copie possédée) pour ajuster les quantités depuis la grille collection. */
@Composable
fun CollectionQuantityStepper(
    item: CollectionItem,
    onAdjust: (slug: String, finish: String, delta: Int) -> Unit,
) {
    if (item.copies.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item.copies.forEach { copy ->
            if (item.copies.size > 1) {
                Text(copy.finish, style = MaterialTheme.typography.labelSmall)
            }
            CompactStepper(
                value = copy.quantity,
                onValueChange = { newValue -> onAdjust(copy.slug, copy.finish, newValue - copy.quantity) },
            )
        }
    }
}

@Composable
private fun CompactStepper(value: Int, onValueChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AdjustButton(
            symbol = "−",
            contentDescription = stringResource(R.string.core_counter_decrement),
            onClick = { onValueChange((value - 1).coerceAtLeast(0)) },
            onLongClick = { onValueChange((value - 5).coerceAtLeast(0)) },
            size = 32.dp,
        )
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        AdjustButton(
            symbol = "+",
            contentDescription = stringResource(R.string.core_counter_increment),
            onClick = { onValueChange(value + 1) },
            onLongClick = { onValueChange(value + 5) },
            size = 32.dp,
        )
    }
}

/** Filtres rapides : possession (Ownership) + élément. */
@Composable
fun QuickFilterChips(
    ownership: Ownership,
    element: Element?,
    onOwnership: (Ownership) -> Unit,
    onElement: (Element?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Ownership.entries.forEach { entry ->
                FilterChip(
                    selected = ownership == entry,
                    onClick = { onOwnership(entry) },
                    label = { Text(ownershipLabel(entry)) },
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
            Element.entries.forEach { entry ->
                FilterChip(
                    selected = element == entry,
                    onClick = { onElement(if (element == entry) null else entry) },
                    label = { Text(entry.name) },
                )
            }
        }
    }
}

/** Filtres avancés repliables : type / rareté / set. */
@Composable
fun AdvancedFilters(
    rarity: Rarity?,
    type: String?,
    setName: String?,
    types: List<String>,
    sets: List<String>,
    onRarity: (Rarity?) -> Unit,
    onType: (String?) -> Unit,
    onSet: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .animateContentSize(),
    ) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(
                if (expanded) stringResource(R.string.collection_hide_advanced_filters)
                else stringResource(R.string.collection_advanced_filters),
            )
        }
        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                types.forEach { entry ->
                    FilterChip(
                        selected = type == entry,
                        onClick = { onType(if (type == entry) null else entry) },
                        label = { Text(entry) },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Rarity.entries.forEach { entry ->
                    FilterChip(
                        selected = rarity == entry,
                        onClick = { onRarity(if (rarity == entry) null else entry) },
                        label = { Text(entry.name) },
                    )
                }
                DropdownFilter(stringResource(R.string.collection_filter_set), setName, sets, onSet)
            }
        }
    }
}

@Composable
private fun DropdownFilter(
    label: String,
    selected: String?,
    options: List<String>,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) { Text(selected ?: label) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.collection_all)) }, onClick = { onSelect(null); expanded = false })
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = { onSelect(option); expanded = false },
            )
        }
    }
}

@Composable
private fun ownershipLabel(ownership: Ownership): String = when (ownership) {
    Ownership.All -> stringResource(R.string.collection_ownership_all)
    Ownership.Owned -> stringResource(R.string.collection_ownership_owned)
    Ownership.Missing -> stringResource(R.string.collection_ownership_missing)
    Ownership.Surplus -> stringResource(R.string.collection_ownership_surplus)
}
