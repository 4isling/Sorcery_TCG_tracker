package com.hayse.sorcery.feature.social.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.social.domain.model.SavedTradeItem
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.ui.viewmodel.SavedTradesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SavedTradesScreen(
    modifier: Modifier = Modifier,
    viewModel: SavedTradesViewModel = koinViewModel(),
) {
    val trades by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    if (trades.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.saved_trades_empty),
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        items(trades, key = { it.id }) { trade ->
            SavedTradeCard(
                trade = trade,
                onApply = { viewModel.apply(trade.id) },
                onDelete = { viewModel.delete(trade.id) },
            )
        }
    }
}

@Composable
private fun SavedTradeCard(trade: SavedTradeItem, onApply: () -> Unit, onDelete: () -> Unit) {
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.saved_trades_with, trade.peerPseudo),
                style = MaterialTheme.typography.titleMedium,
            )
            TradeLines(stringResource(R.string.room_i_give), trade.iGive)
            HorizontalDivider()
            TradeLines(stringResource(R.string.room_i_receive), trade.iReceive)

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                if (trade.done) {
                    Text(
                        text = stringResource(R.string.saved_trades_done),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.saved_trades_apply))
                    }
                }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.saved_trades_delete)) }
            }
        }
    }
}

@Composable
private fun TradeLines(title: String, lines: List<TradeCardLine>) {
    Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    if (lines.isEmpty()) {
        Text(text = "—", style = MaterialTheme.typography.bodyMedium)
    } else {
        lines.forEach { line ->
            Text(
                text = stringResource(R.string.room_offer_line, line.card.name, line.printing.finish, line.quantity),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
