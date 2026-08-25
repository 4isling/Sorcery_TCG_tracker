package com.hayse.sorcery.feature.collection.ui.screen.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard

/** Ligne collection : image + nom + un stepper par copie (impression + finish). */
@Composable
fun CollectionCardRow(
    item: CollectionItem,
    onClick: () -> Unit,
    onAdjust: (slug: String, finish: String, delta: Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = item.card.imageUri,
                contentDescription = item.card.name,
                modifier = Modifier.size(width = 56.dp, height = 78.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.card.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.copies.isEmpty()) {
                    Text(
                        text = "Non possédée",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    item.copies.forEach { copy ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = copy.finish,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            CounterStepper(
                                value = copy.quantity,
                                onValueChange = { newValue ->
                                    onAdjust(copy.slug, copy.finish, newValue - copy.quantity)
                                },
                                min = 0,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Ligne de complétion d'un set : nom + owned/total + barre. Cliquable pour voir les manquants. */
@Composable
fun SetCompletionRow(completion: SetCompletion, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(completion.setName, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${completion.owned} / ${completion.total}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            val fraction = if (completion.total > 0) completion.owned.toFloat() / completion.total else 0f
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
            TextButton(onClick = onClick) { Text("Voir les manquants") }
        }
    }
}

/** Ligne de surplus : nom + possédées/max + surplus. */
@Composable
fun SurplusRow(surplus: SurplusCard) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(surplus.card.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Possédées ${surplus.owned} · playset ${surplus.maxCopies}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "+${surplus.surplus}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
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
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Masquer les filtres avancés" else "Filtres avancés")
        }
        if (expanded) {
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
                DropdownFilter("Type", type, types, onType)
                DropdownFilter("Set", setName, sets, onSet)
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
        DropdownMenuItem(text = { Text("Tous") }, onClick = { onSelect(null); expanded = false })
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = { onSelect(option); expanded = false },
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
