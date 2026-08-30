package com.hayse.sorcery.feature.home.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.home.ui.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    onOpenCards: () -> Unit,
    onOpenCollection: () -> Unit,
    onOpenGameMenu: () -> Unit,
    onOpenDecks: () -> Unit,
    onResumeGame: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        if (state.hasActiveGame) {
            ResumeGameCard(turn = state.activeTurn, onClick = onResumeGame)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
            HomeTile(
                label = "Cartes",
                icon = Icons.Filled.Style,
                onClick = onOpenCards,
                modifier = Modifier.weight(1f),
            )
            HomeTile(
                label = "Collection",
                icon = Icons.Filled.CollectionsBookmark,
                onClick = onOpenCollection,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
            HomeTile(
                label = "Suivi de partie",
                icon = Icons.Filled.SportsEsports,
                onClick = onOpenGameMenu,
                modifier = Modifier.weight(1f),
            )
            HomeTile(
                label = "Decks",
                icon = Icons.Filled.Dashboard,
                onClick = onOpenDecks,
                modifier = Modifier.weight(1f),
            )
        }

        if (state.recentGames.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Dernières parties", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onOpenHistory) { Text("Voir tout") }
            }
            state.recentGames.forEach { record ->
                RecentGameRow(record, onClick = onOpenHistory)
            }
        }
    }
}

@Composable
private fun ResumeGameCard(turn: Int?, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(40.dp))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text("Reprendre la partie", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = turn?.let { "Tour $it" } ?: "Partie en cours",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun HomeTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(onClick = onClick, modifier = modifier.height(120.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecentGameRow(record: GameRecord, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(record.playedAt)),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = playerLabel(record.playerOnePseudo, PlayerId.One) +
                    "  vs  " + playerLabel(record.playerTwoPseudo, PlayerId.Two),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun playerLabel(pseudo: String?, id: PlayerId): String =
    pseudo?.takeIf(String::isNotBlank)
        ?: if (id == PlayerId.One) "Joueur 1" else "Joueur 2"
