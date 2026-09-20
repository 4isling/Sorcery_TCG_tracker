package com.hayse.sorcery.feature.social.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.social.ui.permission.NearbyPermissions
import com.hayse.sorcery.feature.social.ui.screen.composable.RoomInterior
import com.hayse.sorcery.feature.social.ui.viewmodel.GlobalRoomViewModel
import com.hayse.sorcery.feature.social.ui.viewmodel.state.GlobalChatLine
import com.hayse.sorcery.feature.social.ui.viewmodel.state.GlobalRoomViewState
import org.koin.androidx.compose.koinViewModel

@Composable
fun GlobalRoomScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GlobalRoomViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val privateState by viewModel.privateState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissions = remember { NearbyPermissions.required().toTypedArray() }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.onPermissionsResult(NearbyPermissions.allGranted(context)) }

    LaunchedGrantCheck(viewModel = viewModel, context = context)

    // Maillage borné au premier plan : advertise/discover seulement quand l'écran est visible.
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        if (state.permissionsGranted) viewModel.start()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.stop() }

    if (!state.permissionsGranted) {
        PermissionGate(onGrant = { launcher.launch(permissions) }, modifier = modifier)
        return
    }

    val convo = privateState
    if (convo != null) {
        RoomInterior(
            state = convo,
            onCardClick = onCardClick,
            onTabChange = viewModel::privSetTab,
            onSendChat = viewModel::privSendChat,
            onShareCollection = viewModel::privShareCollection,
            onShareLists = viewModel::privShareLists,
            onShareDeck = viewModel::privShareDeck,
            onSavePeerDeck = viewModel::privSavePeerDeck,
            onPropose = viewModel::privPropose,
            onSaveComposed = viewModel::privSaveComposed,
            onRespondOffer = viewModel::privRespondOffer,
            onSaveIncoming = viewModel::privSaveIncoming,
            onLeave = viewModel::closePrivate,
            modifier = modifier,
        )
        return
    }

    GlobalRoom(
        state = state,
        onOpenPrivate = viewModel::openPrivate,
        onSendChat = viewModel::sendGlobalChat,
        modifier = modifier,
    )
}

/** Vérifie les permissions à l'entrée et démarre le maillage une fois accordées. */
@Composable
private fun LaunchedGrantCheck(viewModel: GlobalRoomViewModel, context: android.content.Context) {
    val granted = NearbyPermissions.allGranted(context)
    androidx.compose.runtime.LaunchedEffect(granted) {
        viewModel.onPermissionsResult(granted)
        if (granted) viewModel.start()
    }
}

@Composable
private fun GlobalRoom(
    state: GlobalRoomViewState,
    onOpenPrivate: (String) -> Unit,
    onSendChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.global_participants),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
        )
        if (state.participants.isEmpty()) {
            Text(
                text = stringResource(R.string.global_empty_roster),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.md),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(state.participants, key = { it.id }) { participant ->
                    AssistChip(
                        onClick = { onOpenPrivate(participant.id) },
                        label = { Text(participant.pseudo) },
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.sm))

        GroupChat(messages = state.messages, onSend = onSendChat, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GroupChat(
    messages: List<GlobalChatLine>,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    var draft by remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxSize()) {
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
                items(messages) { line -> GroupBubble(line) }
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
                placeholder = { Text(stringResource(R.string.global_chat_hint)) },
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
private fun GroupBubble(line: GlobalChatLine) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (line.fromMe) Alignment.End else Alignment.Start,
    ) {
        if (!line.fromMe) {
            Text(
                text = line.senderPseudo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = spacing.sm),
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (line.fromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            )
        }
    }
}

@Composable
private fun PermissionGate(onGrant: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxSize().padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.global_permissions_rationale),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.pairing_grant))
        }
    }
}
