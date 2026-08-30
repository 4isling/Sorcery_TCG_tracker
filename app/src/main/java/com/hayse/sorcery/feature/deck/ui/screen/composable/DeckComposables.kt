package com.hayse.sorcery.feature.deck.ui.screen.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.ui.composable.CardImage
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckIssue
import com.hayse.sorcery.feature.deck.domain.model.DeckIssueSeverity
import com.hayse.sorcery.feature.deck.domain.model.DeckSection
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.model.DeckValidation

/** Ligne de la liste des decks : avatar + nom + compteurs + badge de légalité. */
@Composable
fun DeckSummaryRow(
    summary: DeckSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CardImage(
                imageUri = summary.avatarImageUri,
                contentDescription = summary.avatarName,
                modifier = Modifier.size(width = 56.dp, height = 78.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = summary.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary.avatarName ?: "Sans Avatar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${summary.format.label} · ${summary.spellbookCount} sorts · ${summary.atlasCount} sites",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (summary.isLegal) "Légal" else "Incomplet",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (summary.isLegal) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

/** En-tête de section (Avatar / Grimoire / Atlas) avec compteur et minimum éventuel. */
@Composable
fun SectionHeader(title: String, count: Int, minimum: Int? = null, onClick: (() -> Unit)? = null) {
    val label = if (minimum != null) "$count / $minimum" else count.toString()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (onClick != null) "$title +" else title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (minimum != null && count < minimum) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

/** Ligne d'une carte du deck : image + nom + possession + stepper de quantité. */
@Composable
fun DeckEntryRow(
    entry: DeckEntry,
    onClick: () -> Unit,
    onAdjust: (cardName: String, delta: Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CardImage(
                imageUri = entry.card.imageUri,
                contentDescription = entry.card.name,
                modifier = Modifier.size(width = 48.dp, height = 67.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = entry.card.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                OwnershipLabel(entry)
            }
            CounterStepper(
                value = entry.quantity,
                onValueChange = { newValue -> onAdjust(entry.card.name, newValue - entry.quantity) },
                min = 0,
            )
        }
    }
}

/** Texte de possession : possédées vs requises, en rouge s'il en manque. */
@Composable
private fun OwnershipLabel(entry: DeckEntry) {
    val rarity = entry.card.rarity?.name?.let { " · $it" }.orEmpty()
    if (entry.missing > 0) {
        Text(
            text = "Possédées ${entry.owned}/${entry.quantity} · ${entry.missing} à acquérir$rarity",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    } else {
        Text(
            text = "Possédées ${entry.owned}$rarity",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Récapitulatif de validation : "Légal" ou la liste des problèmes bloquants (cliquables pour corriger). */
@Composable
fun ValidationSummary(validation: DeckValidation, onFixIssue: (DeckIssue) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (validation.isLegal) {
            Text(
                text = "Deck légal",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            validation.issues.forEach { issue -> IssueLine(issue, onFixIssue) }
        }
    }
}

/** Filtres du catalogue : possession + élément (rapides) puis type / rareté / set (repliables). */
@Composable
fun DeckFilterBar(
    filter: DeckCatalogFilter,
    types: List<String>,
    sets: List<String>,
    onOwnership: (Ownership) -> Unit,
    onElement: (Element?) -> Unit,
    onType: (String?) -> Unit,
    onRarity: (Rarity?) -> Unit,
    onSet: (String?) -> Unit,
    onClearSection: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        filter.section?.let { section ->
            ChipRow {
                FilterChip(
                    selected = true,
                    onClick = onClearSection,
                    label = { Text("${sectionLabel(section)} ✕") },
                )
            }
        }
        ChipRow {
            Ownership.entries.forEach { entry ->
                FilterChip(
                    selected = filter.ownership == entry,
                    onClick = { onOwnership(entry) },
                    label = { Text(ownershipLabel(entry)) },
                )
            }
        }
        ChipRow {
            Element.entries.forEach { entry ->
                FilterChip(
                    selected = filter.element == entry,
                    onClick = { onElement(if (filter.element == entry) null else entry) },
                    label = { Text(entry.name) },
                )
            }
        }
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Masquer les filtres avancés" else "Filtres avancés")
        }
        if (expanded) {
            ChipRow {
                types.forEach { type ->
                    FilterChip(
                        selected = filter.type == type,
                        onClick = { onType(if (filter.type == type) null else type) },
                        label = { Text(type) },
                    )
                }
            }
            ChipRow {
                Rarity.entries.forEach { entry ->
                    FilterChip(
                        selected = filter.rarity == entry,
                        onClick = { onRarity(if (filter.rarity == entry) null else entry) },
                        label = { Text(entry.name) },
                    )
                }
                DropdownFilter(filter.setName ?: "Set", sets, onSet)
            }
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun DropdownFilter(label: String, options: List<String>, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(label) }
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
}

private fun ownershipLabel(ownership: Ownership): String = when (ownership) {
    Ownership.All -> "Toutes"
    Ownership.Owned -> "Possédées"
    Ownership.Missing -> "Manquantes"
    Ownership.Surplus -> "Surplus"
}

private fun sectionLabel(section: DeckSection): String = when (section) {
    DeckSection.Avatar -> "Avatar"
    DeckSection.Spellbook -> "Grimoire"
    DeckSection.Atlas -> "Atlas"
}

@Composable
private fun IssueLine(issue: DeckIssue, onFixIssue: (DeckIssue) -> Unit) {
    val suffix = if (issue.isFixable) " ›" else ""
    Text(
        text = "• ${issue.message}$suffix",
        style = MaterialTheme.typography.bodySmall,
        color = if (issue.severity == DeckIssueSeverity.Error) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (issue.isFixable) Modifier.clickable { onFixIssue(issue) } else Modifier),
    )
}
