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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
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
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerExpiryAction
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
        onConfirm = { config, timerConfig ->
            viewModel.newGame(config, timerConfig)
            onStarted()
        },
        onCancel = onCancel,
        modifier = modifier,
    )
}

@Composable
private fun GameSetupContent(
    defaultOwnerPseudo: String?,
    onConfirm: (GameConfig, TimerConfig) -> Unit,
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

    val defaults = remember { TimerConfig() }
    var timerEnabled by remember { mutableStateOf(false) }
    var useGlobal by remember { mutableStateOf(defaults.useGlobal) }
    var globalMinutes by remember { mutableIntStateOf(defaults.globalSeconds / 60) }
    var usePerTurn by remember { mutableStateOf(defaults.usePerTurn) }
    var perTurnMinutes by remember { mutableIntStateOf(defaults.perTurnSeconds / 60) }
    var extraTurns by remember { mutableIntStateOf(defaults.extraTurns) }
    var expiry by remember { mutableStateOf(defaults.expiry) }

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

        HorizontalDivider()

        TimerSetupSection(
            enabled = timerEnabled,
            onEnabledChange = { timerEnabled = it },
            useGlobal = useGlobal,
            onUseGlobalChange = { useGlobal = it },
            globalMinutes = globalMinutes,
            onGlobalMinutesChange = { globalMinutes = it },
            usePerTurn = usePerTurn,
            onUsePerTurnChange = { usePerTurn = it },
            perTurnMinutes = perTurnMinutes,
            onPerTurnMinutesChange = { perTurnMinutes = it },
            extraTurns = extraTurns,
            onExtraTurnsChange = { extraTurns = it },
            expiry = expiry,
            onExpiryChange = { expiry = it },
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
                        TimerConfig(
                            enabled = timerEnabled && (useGlobal || usePerTurn),
                            useGlobal = useGlobal,
                            globalSeconds = globalMinutes.coerceAtLeast(1) * 60,
                            usePerTurn = usePerTurn,
                            perTurnSeconds = perTurnMinutes.coerceAtLeast(1) * 60,
                            extraTurns = extraTurns.coerceAtLeast(0),
                            expiry = expiry,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.game_start)) }
        }
    }
}

@Composable
private fun TimerSetupSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    useGlobal: Boolean,
    onUseGlobalChange: (Boolean) -> Unit,
    globalMinutes: Int,
    onGlobalMinutesChange: (Int) -> Unit,
    usePerTurn: Boolean,
    onUsePerTurnChange: (Boolean) -> Unit,
    perTurnMinutes: Int,
    onPerTurnMinutesChange: (Int) -> Unit,
    extraTurns: Int,
    onExtraTurnsChange: (Int) -> Unit,
    expiry: TimerExpiryAction,
    onExpiryChange: (TimerExpiryAction) -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.game_timer_title), style = MaterialTheme.typography.titleMedium)
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }

        if (enabled) {
            TimerToggleRow(
                label = stringResource(R.string.game_timer_global),
                checked = useGlobal,
                onCheckedChange = onUseGlobalChange,
            )
            if (useGlobal) {
                TimerMinutesRow(
                    label = stringResource(R.string.game_timer_minutes),
                    value = globalMinutes,
                    onValueChange = onGlobalMinutesChange,
                )

                Text(
                    text = stringResource(R.string.game_timer_expiry_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                TimerExpiryAction.entries.forEach { option ->
                    ExpiryOptionRow(
                        label = stringResource(expiryLabel(option)),
                        selected = expiry == option,
                        onSelect = { onExpiryChange(option) },
                    )
                }
                if (expiry == TimerExpiryAction.SuddenDeath) {
                    TimerMinutesRow(
                        label = stringResource(R.string.game_timer_extra_turns),
                        value = extraTurns,
                        onValueChange = onExtraTurnsChange,
                        min = 0,
                    )
                }
            }

            TimerToggleRow(
                label = stringResource(R.string.game_timer_per_turn),
                checked = usePerTurn,
                onCheckedChange = onUsePerTurnChange,
            )
            if (usePerTurn) {
                TimerMinutesRow(
                    label = stringResource(R.string.game_timer_minutes),
                    value = perTurnMinutes,
                    onValueChange = onPerTurnMinutesChange,
                )
            }
        }
    }
}

@Composable
private fun TimerToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TimerMinutesRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        CounterStepper(
            value = value,
            onValueChange = onValueChange,
            min = min,
            buttonSize = 36.dp,
            valueStyle = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ExpiryOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@StringRes
private fun expiryLabel(action: TimerExpiryAction): Int = when (action) {
    TimerExpiryAction.SuddenDeath -> R.string.game_timer_expiry_sudden_death
    TimerExpiryAction.Verdict -> R.string.game_timer_expiry_verdict
    TimerExpiryAction.Draw -> R.string.game_timer_expiry_draw
    TimerExpiryAction.None -> R.string.game_timer_expiry_none
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
