package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.LocalWindowWidthSizeClass
import com.hayse.sorcery.core.ui.ProvideTopBarActions
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.isExpanded
import com.hayse.sorcery.core.ui.composable.SorceryDialog
import com.hayse.sorcery.core.ui.theme.LocalSetSkin
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.skin.Skins
import com.hayse.sorcery.core.ui.theme.skin.skinFor
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.GameTimerState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerPhase
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerVerdict
import com.hayse.sorcery.feature.game_tracker.ui.screen.composable.DiceRollerDialog
import com.hayse.sorcery.feature.game_tracker.ui.screen.composable.GameLogSheet
import com.hayse.sorcery.feature.game_tracker.ui.screen.composable.PlayerPanel
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameTrackerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameTrackerScreen(
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameTrackerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Garde l'écran allumé pendant une partie posée sur la table.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    state.game?.let { game ->
        if (state.showDice) {
            DiceRollerDialog(
                game = game,
                onRollDice = viewModel::rollDice,
                onRollFirstPlayer = viewModel::rollFirstPlayer,
                onRollHarbinger = viewModel::rollHarbinger,
                onDismiss = { viewModel.showDice(false) },
            )
        }
        if (state.showLog) {
            GameLogSheet(game = game, onDismiss = { viewModel.showLog(false) })
        }
    }

    when {
        state.loading -> LoadingState(modifier)

        state.game == null -> {
            NoGameCta(modifier = modifier, onNewGame = onNewGame)
        }

        else -> {
            GameContent(
                game = state.game!!,
                viewModel = viewModel,
                playerOneSet = state.playerOneAvatarSet,
                playerTwoSet = state.playerTwoAvatarSet,
                timer = state.timer,
                verdict = state.verdict,
                onNewGame = onNewGame,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun NoGameCta(modifier: Modifier = Modifier, onNewGame: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.md),
        ) {
            Text(stringResource(R.string.game_no_game), style = MaterialTheme.typography.titleLarge)
            Button(onClick = onNewGame) { Text(stringResource(R.string.game_new_game)) }
        }
    }
}

@Composable
private fun GameContent(
    game: GameState,
    viewModel: GameTrackerViewModel,
    playerOneSet: String?,
    playerTwoSet: String?,
    timer: GameTimerState?,
    verdict: TimerVerdict?,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (verdict != null) {
        VerdictDialog(
            verdict = verdict,
            playerOnePseudo = game.player(PlayerId.One).pseudo,
            playerTwoPseudo = game.player(PlayerId.Two).pseudo,
            onDismiss = viewModel::dismissVerdict,
        )
    }

    var showEndConfirm by remember { mutableStateOf(false) }

    // Actions de partie déportées dans la barre supérieure pour alléger la barre de contrôle.
    ProvideTopBarActions {
        IconButton(onClick = { viewModel.showDice(true) }) {
            Icon(Icons.Filled.Casino, contentDescription = stringResource(R.string.game_dice))
        }
        IconButton(onClick = { viewModel.showLog(true) }) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.game_log))
        }
        IconButton(onClick = onNewGame) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.game_new_game))
        }
        IconButton(onClick = { showEndConfirm = true }) {
            Icon(Icons.Filled.Flag, contentDescription = stringResource(R.string.game_finish))
        }
    }

    if (showEndConfirm) {
        SorceryDialog(
            title = stringResource(R.string.game_end_confirm_title),
            confirmText = stringResource(R.string.game_finish),
            onConfirm = {
                showEndConfirm = false
                viewModel.endGame()
            },
            dismissText = stringResource(R.string.game_cancel),
            onDismiss = { showEndConfirm = false },
        ) {
            Text(stringResource(R.string.game_end_confirm_message))
        }
    }

    val controlBar: @Composable () -> Unit = {
        ControlBar(
            turn = game.turn,
            timer = timer,
            onToggleTimer = viewModel::toggleTimer,
            onNewTurn = viewModel::newTurn,
            onUndo = viewModel::undo,
        )
    }

    if (LocalWindowWidthSizeClass.current.isExpanded) {
        // Tablette paysage : joueurs côte-à-côte (sans rotation), barre de contrôle en bas.
        Column(modifier = modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                PlayerScrollBox(modifier = Modifier.weight(1f)) {
                    OpponentPanel(game, viewModel, playerTwoSet)
                }
                VerticalDivider()
                PlayerScrollBox(modifier = Modifier.weight(1f)) {
                    SelfPanel(game, viewModel, playerOneSet)
                }
            }
            controlBar()
        }
    } else {
        // Téléphone / portrait : vis-à-vis vertical, adversaire retourné à 180°, barre au centre.
        Column(modifier = modifier.fillMaxSize()) {
            PlayerScrollBox(modifier = Modifier.weight(1f)) {
                OpponentPanel(game, viewModel, playerTwoSet, modifier = Modifier.rotate(180f))
            }
            controlBar()
            PlayerScrollBox(modifier = Modifier.weight(1f)) {
                SelfPanel(game, viewModel, playerOneSet)
            }
        }
    }
}

@Composable
private fun ColumnScope.PlayerScrollBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun RowScope.PlayerScrollBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Applique l'identité visuelle du set de l'avatar au panneau (décoration neutre en mode Off). */
@Composable
private fun SetSkinnedPanel(setName: String?, content: @Composable () -> Unit) {
    val skinEnabled = LocalSetSkin.current != Skins.Default
    val set = sorcerySetFromName(setName)
    SorceryTheme(set = set, skin = if (skinEnabled) skinFor(set) else Skins.Default) {
        content()
    }
}

@Composable
private fun OpponentPanel(
    game: GameState,
    viewModel: GameTrackerViewModel,
    setName: String?,
    modifier: Modifier = Modifier,
) {
    SetSkinnedPanel(setName) {
        PlayerPanel(
            title = stringResource(R.string.game_player_two),
            isActive = game.activePlayer == PlayerId.Two,
            player = game.player(PlayerId.Two),
            onDamage = { viewModel.damage(PlayerId.Two, it) },
            onLifeGain = { viewModel.lifeGain(PlayerId.Two, it) },
            onManaDelta = { viewModel.manaDelta(PlayerId.Two, it) },
            onSiteDelta = { viewModel.siteDelta(PlayerId.Two, it) },
            onAffinityDelta = { element, delta -> viewModel.affinityDelta(PlayerId.Two, element, delta) },
            modifier = modifier,
        )
    }
}

@Composable
private fun SelfPanel(
    game: GameState,
    viewModel: GameTrackerViewModel,
    setName: String?,
    modifier: Modifier = Modifier,
) {
    SetSkinnedPanel(setName) {
        PlayerPanel(
            title = stringResource(R.string.game_player_one),
            isActive = game.activePlayer == PlayerId.One,
            player = game.player(PlayerId.One),
            onDamage = { viewModel.damage(PlayerId.One, it) },
            onLifeGain = { viewModel.lifeGain(PlayerId.One, it) },
            onManaDelta = { viewModel.manaDelta(PlayerId.One, it) },
            onSiteDelta = { viewModel.siteDelta(PlayerId.One, it) },
            onAffinityDelta = { element, delta -> viewModel.affinityDelta(PlayerId.One, element, delta) },
            modifier = modifier,
        )
    }
}

@Composable
private fun ControlBar(
    turn: Int,
    timer: GameTimerState?,
    onToggleTimer: () -> Unit,
    onNewTurn: () -> Unit,
    onUndo: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Surface(tonalElevation = 3.dp, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(stringResource(R.string.game_turn_n, turn), style = MaterialTheme.typography.titleMedium)
            if (timer != null) {
                TimerInline(timer = timer, onToggle = onToggleTimer, modifier = Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
            IconButton(onClick = onUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.game_undo))
            }
            FilledTonalIconButton(onClick = onNewTurn) {
                Icon(Icons.Filled.NavigateNext, contentDescription = stringResource(R.string.game_next_turn))
            }
        }
    }
}

/** Chrono en ligne : temps restant (global / par tour), état de mort subite, et pause/reprise. */
@Composable
private fun TimerInline(timer: GameTimerState, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val suddenDeath = timer.phase == TimerPhase.SuddenDeath
    val finished = timer.phase == TimerPhase.Finished
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
    ) {
        val accent = when {
            finished -> MaterialTheme.colorScheme.error
            suddenDeath -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }
        when {
            finished -> Text(
                stringResource(R.string.game_timer_finished),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
            suddenDeath -> Text(
                stringResource(R.string.game_timer_sudden_death, timer.extraTurnsLeft),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
            timer.config.useGlobal -> TimerChip(
                label = stringResource(R.string.game_timer_global_short),
                seconds = timer.globalRemaining,
                color = accent,
            )
        }
        if (timer.config.usePerTurn && !finished) {
            TimerChip(
                label = stringResource(R.string.game_timer_turn_short),
                seconds = timer.turnRemaining,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        if (!finished) {
            IconButton(onClick = onToggle) {
                if (timer.running) {
                    Icon(Icons.Filled.Pause, contentDescription = stringResource(R.string.game_timer_pause))
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.game_timer_resume))
                }
            }
        }
    }
}

@Composable
private fun TimerChip(label: String, seconds: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.xs),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatClock(seconds), style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun VerdictDialog(
    verdict: TimerVerdict,
    playerOnePseudo: String?,
    playerTwoPseudo: String?,
    onDismiss: () -> Unit,
) {
    val p1 = playerOnePseudo ?: stringResource(R.string.game_player_one)
    val p2 = playerTwoPseudo ?: stringResource(R.string.game_player_two)
    val message = when (verdict) {
        TimerVerdict.PlayerOneWins -> stringResource(R.string.game_timer_verdict_winner, p1)
        TimerVerdict.PlayerTwoWins -> stringResource(R.string.game_timer_verdict_winner, p2)
        TimerVerdict.Draw -> stringResource(R.string.game_timer_verdict_draw)
    }
    SorceryDialog(
        title = stringResource(R.string.game_timer_verdict_title),
        confirmText = stringResource(R.string.game_timer_verdict_ok),
        onConfirm = onDismiss,
        onDismiss = onDismiss,
    ) {
        Text(message)
    }
}

private fun formatClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
