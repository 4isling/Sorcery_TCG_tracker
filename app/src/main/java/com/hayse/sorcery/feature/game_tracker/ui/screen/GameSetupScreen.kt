package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.CardImage
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerIdentity
import com.hayse.sorcery.feature.game_tracker.ui.screen.composable.AvatarPickerDialog
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameTrackerViewModel
import org.koin.androidx.compose.koinViewModel

/** Écran plein de configuration d'une nouvelle partie (transposé de l'ancien NewGameDialog). */
@Composable
fun GameSetupScreen(
    onStarted: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameTrackerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GameSetupContent(
        defaultOwnerPseudo = state.defaultOwnerPseudo,
        onConfirm = { config ->
            viewModel.newGame(config)
            onStarted()
        },
        onCancel = onCancel,
        modifier = modifier,
    )
}

@Composable
private fun GameSetupContent(
    defaultOwnerPseudo: String?,
    onConfirm: (GameConfig) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    var startingLife by remember { mutableIntStateOf(GameConfig().startingLife) }
    var p1Avatar by remember { mutableStateOf<PlayerIdentity?>(null) }
    var p2Avatar by remember { mutableStateOf<PlayerIdentity?>(null) }
    var p1Pseudo by remember { mutableStateOf(defaultOwnerPseudo.orEmpty()) }
    var p2Pseudo by remember { mutableStateOf("") }
    var pickerFor by remember { mutableStateOf<PlayerId?>(null) }

    pickerFor?.let { player ->
        AvatarPickerDialog(
            onPick = { identity ->
                if (player == PlayerId.One) p1Avatar = identity else p2Avatar = identity
                pickerFor = null
            },
            onDismiss = { pickerFor = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(stringResource(R.string.game_starting_life), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CounterStepper(value = startingLife, onValueChange = { startingLife = it }, min = 1)
        }

        PlayerSetupRow(
            label = stringResource(R.string.game_player_one),
            avatar = p1Avatar,
            pseudo = p1Pseudo,
            onPseudoChange = { p1Pseudo = it },
            onPickAvatar = { pickerFor = PlayerId.One },
        )
        PlayerSetupRow(
            label = stringResource(R.string.game_player_two),
            avatar = p2Avatar,
            pseudo = p2Pseudo,
            onPseudoChange = { p2Pseudo = it },
            onPickAvatar = { pickerFor = PlayerId.Two },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.game_cancel)) }
            Button(
                onClick = {
                    onConfirm(
                        GameConfig(
                            startingLife = startingLife,
                            playerOne = identityOf(p1Avatar, p1Pseudo),
                            playerTwo = identityOf(p2Avatar, p2Pseudo),
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.game_start)) }
        }
    }
}

private fun identityOf(avatar: PlayerIdentity?, pseudo: String): PlayerIdentity? {
    val trimmed = pseudo.trim().ifBlank { null }
    if (avatar == null && trimmed == null) return null
    return PlayerIdentity(
        avatarName = avatar?.avatarName,
        avatarImageUri = avatar?.avatarImageUri,
        pseudo = trimmed,
    )
}

@Composable
private fun PlayerSetupRow(
    label: String,
    avatar: PlayerIdentity?,
    pseudo: String,
    onPseudoChange: (String) -> Unit,
    onPickAvatar: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (avatar?.avatarImageUri != null) {
                CardImage(
                    imageUri = avatar.avatarImageUri,
                    contentDescription = avatar.avatarName,
                    modifier = Modifier.size(width = 40.dp, height = 56.dp),
                )
            }
            OutlinedButton(onClick = onPickAvatar) {
                Text(avatar?.avatarName ?: stringResource(R.string.game_choose_avatar))
            }
        }
        OutlinedTextField(
            value = pseudo,
            onValueChange = onPseudoChange,
            label = { Text(stringResource(R.string.game_pseudo_optional)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
