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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.ui.composable.CardImage
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.core.ui.theme.skin.SkinnedCard
import com.hayse.sorcery.core.ui.theme.skin.SkinnedSectionTitle
import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckIssue
import com.hayse.sorcery.feature.deck.domain.model.DeckIssueMessage
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
    SkinnedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
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
                    text = summary.avatarName ?: stringResource(R.string.deck_no_avatar),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.deck_summary_counts,
                        summary.format.label,
                        summary.spellbookCount,
                        summary.atlasCount,
                        summary.collectionCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (summary.isLegal) stringResource(R.string.deck_legal) else stringResource(R.string.deck_incomplete),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (summary.isLegal) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.deck_delete))
            }
        }
    }
}

/**
 * En-tête de section (Avatar / Grimoire / Atlas / Collection) avec compteur et borne éventuelle :
 * [minimum] à atteindre (deck principal) ou [maximum] à ne pas dépasser (Collection).
 */
@Composable
fun SectionHeader(
    title: String,
    count: Int,
    minimum: Int? = null,
    maximum: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val bound = minimum ?: maximum
    val label = if (bound != null) "$count / $bound" else count.toString()
    val outOfBounds = (minimum != null && count < minimum) || (maximum != null && count > maximum)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkinnedSectionTitle(
            text = if (onClick != null) "$title +" else title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (outOfBounds) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Ligne d'une carte du deck : image + nom + possession + stepper de quantité (dans la zone de l'entrée). */
@Composable
fun DeckEntryRow(
    entry: DeckEntry,
    onClick: () -> Unit,
    onAdjust: (entry: DeckEntry, delta: Int) -> Unit,
) {
    SkinnedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
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
                onValueChange = { newValue -> onAdjust(entry, newValue - entry.quantity) },
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
            text = stringResource(
                R.string.deck_owned_missing,
                entry.owned,
                entry.quantity,
                entry.missing,
                rarity,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    } else {
        Text(
            text = stringResource(R.string.deck_owned, entry.owned, rarity),
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
                text = stringResource(R.string.deck_valid),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            validation.issues.forEach { issue -> IssueLine(issue, onFixIssue) }
        }
    }
}

/** Filtres du catalogue : zone cible, possession + élément (rapides) puis type / rareté / set (repliables). */
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
    onSection: (DeckSection?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Zone cible : filtre le catalogue et détermine où le + ajoute la carte (Collection = réserve).
        ChipRow {
            DeckSection.entries.forEach { section ->
                FilterChip(
                    selected = filter.section == section,
                    onClick = { onSection(if (filter.section == section) null else section) },
                    label = { Text(sectionLabel(section)) },
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
            Text(
                if (expanded) {
                    stringResource(R.string.deck_hide_advanced_filters)
                } else {
                    stringResource(R.string.deck_advanced_filters)
                },
            )
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
                DropdownFilter(filter.setName ?: stringResource(R.string.deck_set), sets, onSet)
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
            DropdownMenuItem(text = { Text(stringResource(R.string.deck_all)) }, onClick = { onSelect(null); expanded = false })
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ownershipLabel(ownership: Ownership): String = when (ownership) {
    Ownership.All -> stringResource(R.string.deck_ownership_all)
    Ownership.Owned -> stringResource(R.string.deck_ownership_owned)
    Ownership.Missing -> stringResource(R.string.deck_ownership_missing)
    Ownership.Surplus -> stringResource(R.string.deck_ownership_surplus)
}

@Composable
private fun sectionLabel(section: DeckSection): String = when (section) {
    DeckSection.Avatar -> stringResource(R.string.deck_section_avatar)
    DeckSection.Spellbook -> stringResource(R.string.deck_section_spellbook)
    DeckSection.Atlas -> stringResource(R.string.deck_section_atlas)
    DeckSection.Collection -> stringResource(R.string.deck_section_collection)
}

@Composable
private fun DeckIssueMessage.text(): String = when (this) {
    DeckIssueMessage.AvatarRequired -> stringResource(R.string.deck_issue_avatar_required)
    is DeckIssueMessage.TooManyAvatars -> stringResource(R.string.deck_issue_too_many_avatars, count)
    is DeckIssueMessage.SpellbookTooSmall -> stringResource(R.string.deck_issue_spellbook_min, count, min)
    is DeckIssueMessage.AtlasTooSmall -> stringResource(R.string.deck_issue_atlas_min, count, min)
    is DeckIssueMessage.CollectionTooLarge -> stringResource(R.string.deck_issue_collection_max, count, max)
    is DeckIssueMessage.BannedRarity -> stringResource(R.string.deck_issue_banned_rarity, cardName, rarity)
    is DeckIssueMessage.TooManyCopies -> stringResource(R.string.deck_issue_copies_max, cardName, quantity, limit)
}

@Composable
private fun IssueLine(issue: DeckIssue, onFixIssue: (DeckIssue) -> Unit) {
    val suffix = if (issue.isFixable) " ›" else ""
    Text(
        text = "• ${issue.message.text()}$suffix",
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
