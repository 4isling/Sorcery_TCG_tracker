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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.composable.SorceryDialog
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
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
                title = "Aucun deck",
                subtitle = "Crée ton premier deck avec le bouton +.",
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.decks, key = { it.id }) { summary ->
                    DeckSummaryRow(
                        summary = summary,
                        onClick = { onOpenDeck(summary.id) },
                        onDelete = { viewModel.delete(summary.id) },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(spacing.md),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nouveau deck")
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
    var format by remember { mutableStateOf(DeckFormat.Sorcery) }
    val spacing = LocalSpacing.current

    SorceryDialog(
        title = "Nouveau deck",
        onDismiss = onDismiss,
        confirmText = "Créer",
        onConfirm = { onConfirm(name, format) },
        dismissText = "Annuler",
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du deck") },
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
        Text("Format : ${selected.label}")
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
