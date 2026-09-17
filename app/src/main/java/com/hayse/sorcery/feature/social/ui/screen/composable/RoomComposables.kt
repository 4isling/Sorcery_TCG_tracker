package com.hayse.sorcery.feature.social.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.AdjustButton
import com.hayse.sorcery.core.ui.composable.CardGridItem
import com.hayse.sorcery.core.ui.composable.CountBadge
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.SuggestionReason
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.ui.viewmodel.state.OfferView
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomTab
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomViewState
import com.hayse.sorcery.feature.social.ui.viewmodel.state.SuggestionCardLine

private val TileWidth = 128.dp

/** Coquille de la room une fois connectée : en-tête, onglets et offre reçue. */
@Composable
fun RoomInterior(
    state: RoomViewState,
    onCardClick: (String) -> Unit,
    onTabChange: (RoomTab) -> Unit,
    onSendChat: (String) -> Unit,
    onShareCollection: () -> Unit,
    onShareLists: () -> Unit,
    onPropose: (List<MatchLine>, List<MatchLine>) -> Unit,
    onSaveComposed: (List<MatchLine>, List<MatchLine>) -> Unit,
    onRespondOffer: (Boolean) -> Unit,
    onSaveIncoming: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.peerPseudo?.let { stringResource(R.string.room_with, it) }
                    ?: stringResource(R.string.room_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onLeave) { Text(stringResource(R.string.room_leave)) }
        }

        SecondaryTabRow(selectedTabIndex = state.roomTab.ordinal) {
            RoomTab.entries.forEach { tab ->
                Tab(
                    selected = state.roomTab == tab,
                    onClick = { onTabChange(tab) },
                    text = { Text(roomTabLabel(tab)) },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (state.roomTab) {
                RoomTab.Chat -> ChatTab(state.messages, onSendChat)
                RoomTab.PeerCollection -> PeerCollectionTab(state, onCardClick, onShareCollection, onShareLists)
                RoomTab.Trade -> TradeTab(state, onPropose, onSaveComposed)
                RoomTab.Suggestions -> SuggestionsTab(state, onPropose)
            }
        }
    }

    state.incomingOffer?.let { offer ->
        OfferDialog(
            offer = offer,
            onAccept = { onRespondOffer(true) },
            onDecline = { onRespondOffer(false) },
            onSave = onSaveIncoming,
        )
    }
}

@Composable
private fun ChatTab(messages: List<com.hayse.sorcery.feature.social.ui.viewmodel.state.ChatLine>, onSend: (String) -> Unit) {
    val spacing = LocalSpacing.current
    var draft by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                EmptyState(title = stringResource(R.string.room_no_messages))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                items(messages) { line -> ChatBubble(line.fromMe, line.text) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(stringResource(R.string.room_chat_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSend(draft)
                        draft = ""
                    }
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.room_send))
            }
        }
    }
}

@Composable
private fun ChatBubble(fromMe: Boolean, text: String) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (fromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            )
        }
    }
}

@Composable
private fun PeerCollectionTab(
    state: RoomViewState,
    onCardClick: (String) -> Unit,
    onShareCollection: () -> Unit,
    onShareLists: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            OutlinedButton(onClick = onShareCollection, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.room_share_collection))
            }
            OutlinedButton(onClick = onShareLists, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.room_share_lists))
            }
        }
        if (state.peerCollection.isEmpty()) {
            EmptyState(title = stringResource(R.string.room_peer_collection_empty))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(state.peerCollection, key = { it.printing.slug }) { line ->
                    CardGridItem(
                        imageUri = line.printing.imageUri,
                        name = line.card.name,
                        onClick = { onCardClick(line.card.name) },
                        badge = { CountBadge("×${line.quantity}") },
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeTab(
    state: RoomViewState,
    onPropose: (List<MatchLine>, List<MatchLine>) -> Unit,
    onSaveComposed: (List<MatchLine>, List<MatchLine>) -> Unit,
) {
    val spacing = LocalSpacing.current
    val give = remember { mutableStateMapOf<String, Int>() }
    val receive = remember { mutableStateMapOf<String, Int>() }

    val giveLines = buildLines(state.myCollection, give)
    val receiveLines = buildLines(state.peerCollection, receive)
    val canSubmit = giveLines.isNotEmpty() || receiveLines.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        SelectionRow(
            title = stringResource(R.string.room_i_give),
            lines = state.myCollection,
            selection = give,
            emptyText = stringResource(R.string.room_no_collection),
        )
        SelectionRow(
            title = stringResource(R.string.room_i_receive),
            lines = state.peerCollection,
            selection = receive,
            emptyText = stringResource(R.string.room_peer_collection_empty),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            OutlinedButton(
                onClick = { onSaveComposed(giveLines, receiveLines) },
                enabled = canSubmit,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.room_save_later))
            }
            androidx.compose.material3.Button(
                onClick = {
                    onPropose(giveLines, receiveLines)
                    give.clear()
                    receive.clear()
                },
                enabled = canSubmit,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.room_propose))
            }
        }
    }
}

/**
 * Onglet Suggestions : échanges calculés automatiquement depuis les collections complètes
 * (surplus / « à échanger » ↔ besoins priorisés). Sélection par stepper puis « Proposer ».
 */
@Composable
private fun SuggestionsTab(
    state: RoomViewState,
    onPropose: (List<MatchLine>, List<MatchLine>) -> Unit,
) {
    val spacing = LocalSpacing.current
    val result = state.suggestions
    if (result == null || !result.hasAny) {
        EmptyState(title = stringResource(R.string.suggestion_empty))
        return
    }

    val give = remember { mutableStateMapOf<String, Int>() }
    val receive = remember { mutableStateMapOf<String, Int>() }
    val giveLines = buildSuggestionLines(result.iCanGive, give)
    val receiveLines = buildSuggestionLines(result.iCanReceive, receive)
    val canSubmit = giveLines.isNotEmpty() || receiveLines.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        SuggestionSection(
            title = stringResource(R.string.suggestion_give),
            lines = result.iCanGive,
            selection = give,
            emptyText = stringResource(R.string.suggestion_none_give),
        )
        SuggestionSection(
            title = stringResource(R.string.suggestion_receive),
            lines = result.iCanReceive,
            selection = receive,
            emptyText = stringResource(R.string.suggestion_none_receive),
        )
        androidx.compose.material3.Button(
            onClick = {
                onPropose(giveLines, receiveLines)
                give.clear()
                receive.clear()
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.md),
        ) {
            Text(stringResource(R.string.room_propose))
        }
    }
}

@Composable
private fun SuggestionSection(
    title: String,
    lines: List<SuggestionCardLine>,
    selection: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Int>,
    emptyText: String,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = spacing.md),
        )
        if (lines.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.md),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(lines, key = { it.line.printing.slug }) { suggestion ->
                    val line = suggestion.line
                    val selected = selection[line.printing.slug] ?: 0
                    Column(Modifier.width(TileWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                        CardGridItem(
                            imageUri = line.printing.imageUri,
                            name = line.card.name,
                            onClick = {},
                            badge = { if (selected > 0) CountBadge("×$selected") },
                        )
                        Text(
                            text = suggestionReasonLabel(suggestion.reason),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = "${line.printing.finish} · ${line.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        CompactStepperPublic(
                            value = selected,
                            min = 0,
                            max = line.quantity,
                            onValueChange = { selection[line.printing.slug] = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun suggestionReasonLabel(reason: SuggestionReason): String = when (reason) {
    SuggestionReason.Wanted -> stringResource(R.string.suggestion_reason_wanted)
    SuggestionReason.CompletesSet -> stringResource(R.string.suggestion_reason_set)
    SuggestionReason.Missing -> stringResource(R.string.suggestion_reason_missing)
}

private fun buildSuggestionLines(
    lines: List<SuggestionCardLine>,
    selection: Map<String, Int>,
): List<MatchLine> = lines.mapNotNull { suggestion ->
    val printing = suggestion.line.printing
    val qty = selection[printing.slug]?.takeIf { it > 0 } ?: return@mapNotNull null
    MatchLine(printing.slug, printing.finish, qty)
}

@Composable
private fun SelectionRow(
    title: String,
    lines: List<TradeCardLine>,
    selection: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Int>,
    emptyText: String,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = spacing.md),
        )
        if (lines.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.md),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(lines, key = { it.printing.slug }) { line ->
                    val selected = selection[line.printing.slug] ?: 0
                    Column(Modifier.width(TileWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                        CardGridItem(
                            imageUri = line.printing.imageUri,
                            name = line.card.name,
                            onClick = {},
                            badge = { if (selected > 0) CountBadge("×$selected") },
                        )
                        Text(
                            text = "${line.printing.finish} · ${line.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        CompactStepperPublic(
                            value = selected,
                            min = 0,
                            max = line.quantity,
                            onValueChange = { selection[line.printing.slug] = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferDialog(offer: OfferView, onAccept: () -> Unit, onDecline: () -> Unit, onSave: () -> Unit) {
    val spacing = LocalSpacing.current
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.room_offer_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                OfferLines(stringResource(R.string.room_i_give), offer.iGive)
                HorizontalDivider()
                OfferLines(stringResource(R.string.room_i_receive), offer.iReceive)
                TextButton(onClick = onSave) { Text(stringResource(R.string.room_offer_save)) }
            }
        },
        confirmButton = { TextButton(onClick = onAccept) { Text(stringResource(R.string.room_offer_accept)) } },
        dismissButton = { TextButton(onClick = onDecline) { Text(stringResource(R.string.room_offer_decline)) } },
    )
}

@Composable
private fun OfferLines(title: String, lines: List<TradeCardLine>) {
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

/** Stepper compact borné, réutilisable dans les tuiles de composition d'échange. */
@Composable
private fun CompactStepperPublic(value: Int, min: Int, max: Int, onValueChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AdjustButton(
            symbol = "−",
            contentDescription = stringResource(R.string.core_counter_decrement),
            onClick = { onValueChange((value - 1).coerceIn(min, max)) },
            onLongClick = { onValueChange((value - 5).coerceIn(min, max)) },
            size = 28.dp,
        )
        Text(value.toString(), style = MaterialTheme.typography.titleSmall)
        AdjustButton(
            symbol = "+",
            contentDescription = stringResource(R.string.core_counter_increment),
            onClick = { onValueChange((value + 1).coerceIn(min, max)) },
            onLongClick = { onValueChange((value + 5).coerceIn(min, max)) },
            size = 28.dp,
        )
    }
}

private fun buildLines(
    lines: List<TradeCardLine>,
    selection: Map<String, Int>,
): List<MatchLine> = lines.mapNotNull { line ->
    val qty = selection[line.printing.slug]?.takeIf { it > 0 } ?: return@mapNotNull null
    MatchLine(line.printing.slug, line.printing.finish, qty)
}

@Composable
private fun roomTabLabel(tab: RoomTab): String = when (tab) {
    RoomTab.Chat -> stringResource(R.string.room_tab_chat)
    RoomTab.PeerCollection -> stringResource(R.string.room_tab_peer_collection)
    RoomTab.Trade -> stringResource(R.string.room_tab_trade)
    RoomTab.Suggestions -> stringResource(R.string.room_suggestions)
}
