package com.hayse.sorcery.feature.deck.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.composable.SorceryDialog
import com.hayse.sorcery.core.ui.theme.LocalSetSkin
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.skin.Skins
import com.hayse.sorcery.core.ui.theme.skin.skinFor
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.ui.screen.composable.DeckSummaryRow
import com.hayse.sorcery.feature.deck.ui.viewmodel.DeckListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeckListScreen(
    onOpenDeck: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeckListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    var showCreate by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.loading -> LoadingState()
            state.decks.isEmpty() -> EmptyState(
                title = stringResource(R.string.deck_list_empty_title),
                subtitle = stringResource(R.string.deck_list_empty_subtitle),
            )
            else -> {
                // Décoration désactivée si le skin ambiant est neutre (mode Off).
                val skinEnabled = LocalSetSkin.current != Skins.Default
                LazyColumn(
                    contentPadding = PaddingValues(spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.decks, key = { it.id }) { summary ->
                        val set = sorcerySetFromName(summary.avatarSetName)
                        SorceryTheme(set = set, skin = if (skinEnabled) skinFor(set) else Skins.Default) {
                            DeckSummaryRow(
                                summary = summary,
                                onClick = { onOpenDeck(summary.id) },
                                onDelete = { viewModel.delete(summary.id) },
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(spacing.md),
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.deck_new))
        }
    }

    if (showCreate) {
        CreateDeckDialog(
            onDismiss = { showCreate = false },
            onConfirm = { name, format ->
                showCreate = false
                viewModel.create(name, format, onCreated = onOpenDeck)
            },
        )
    }
}

@Composable
private fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, DeckFormat) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var format by remember { mutableStateOf(DeckFormat.Constructed) }
    val spacing = LocalSpacing.current

    SorceryDialog(
        title = stringResource(R.string.deck_new),
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.deck_create),
        onConfirm = { onConfirm(name, format) },
        dismissText = stringResource(R.string.deck_cancel),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.deck_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            FormatDropdown(selected = format, onSelect = { format = it })
        }
    }
}

@Composable
private fun FormatDropdown(
    selected: DeckFormat,
    onSelect: (DeckFormat) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.deck_format_label, selected.label))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DeckFormat.all.forEach { format ->
            DropdownMenuItem(
                text = { Text(format.label) },
                onClick = {
                    onSelect(format)
                    expanded = false
                },
            )
        }
    }
}
