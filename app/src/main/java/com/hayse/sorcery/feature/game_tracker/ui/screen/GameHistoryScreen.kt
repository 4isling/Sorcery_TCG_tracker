package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameHistoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun GameHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: GameHistoryViewModel = koinViewModel(),
) {
    val games by viewModel.games.collectAsStateWithLifecycle()

    if (games.isEmpty()) {
        EmptyState(title = "Aucune partie terminée", modifier = modifier)
        return
    }

    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        items(games, key = { it.playedAt }) { record ->
            GameRecordCard(record)
        }
    }
}

@Composable
private fun GameRecordCard(record: GameRecord) {
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(record.playedAt)),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "${playerLabel(record.playerOnePseudo, record.playerOneAvatar, PlayerId.One)}" +
                    "  vs  " +
                    playerLabel(record.playerTwoPseudo, record.playerTwoAvatar, PlayerId.Two),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = winnerLabel(record),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("${record.turns} tour(s)", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun playerLabel(pseudo: String?, avatar: String?, id: PlayerId): String {
    val name = pseudo?.takeIf(String::isNotBlank)
        ?: if (id == PlayerId.One) "Joueur 1" else "Joueur 2"
    return if (avatar != null) "$name ($avatar)" else name
}

private fun winnerLabel(record: GameRecord): String = when (record.winner) {
    PlayerId.One -> "Vainqueur : ${playerLabel(record.playerOnePseudo, record.playerOneAvatar, PlayerId.One)}"
    PlayerId.Two -> "Vainqueur : ${playerLabel(record.playerTwoPseudo, record.playerTwoAvatar, PlayerId.Two)}"
    null -> "Sans vainqueur"
}
