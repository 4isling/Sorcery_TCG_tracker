package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.LocalWindowWidthSizeClass
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.isExpanded
import com.hayse.sorcery.core.ui.composable.SorceryDialog
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
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
            Text("Aucune partie en cours", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onNewGame) { Text("Nouvelle partie") }
        }
    }
}

@Composable
private fun GameContent(
    game: GameState,
    viewModel: GameTrackerViewModel,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controlBar: @Composable () -> Unit = {
        ControlBar(
            turn = game.turn,
            onNewTurn = viewModel::newTurn,
            onUndo = viewModel::undo,
            onNewGame = onNewGame,
            onEndGame = viewModel::endGame,
            onDice = { viewModel.showDice(true) },
            onLog = { viewModel.showLog(true) },
        )
    }

    if (LocalWindowWidthSizeClass.current.isExpanded) {
        // Tablette paysage : joueurs côte-à-côte (sans rotation), barre de contrôle en bas.
        Column(modifier = modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                PlayerScrollBox(modifier = Modifier.weight(1f)) {
                    OpponentPanel(game, viewModel)
                }
                VerticalDivider()
                PlayerScrollBox(modifier = Modifier.weight(1f)) {
                    SelfPanel(game, viewModel)
                }
            }
            controlBar()
        }
    } else {
        // Téléphone / portrait : vis-à-vis vertical, adversaire retourné à 180°, barre au centre.
        Column(modifier = modifier.fillMaxSize()) {
            PlayerScrollBox(modifier = Modifier.weight(1f)) {
                OpponentPanel(game, viewModel, modifier = Modifier.rotate(180f))
            }
            controlBar()
            PlayerScrollBox(modifier = Modifier.weight(1f)) {
                SelfPanel(game, viewModel)
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

@Composable
private fun OpponentPanel(game: GameState, viewModel: GameTrackerViewModel, modifier: Modifier = Modifier) {
    PlayerPanel(
        title = "Joueur 2",
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

@Composable
private fun SelfPanel(game: GameState, viewModel: GameTrackerViewModel, modifier: Modifier = Modifier) {
    PlayerPanel(
        title = "Joueur 1",
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

@Composable
private fun ControlBar(
    turn: Int,
    onNewTurn: () -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    onEndGame: () -> Unit,
    onDice: () -> Unit,
    onLog: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var showEndConfirm by remember { mutableStateOf(false) }

    Surface(tonalElevation = 3.dp, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Tour $turn", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                FilledTonalIconButton(onClick = onNewTurn) {
                    Icon(Icons.Filled.NavigateNext, contentDescription = "Tour suivant")
                }
                IconButton(onClick = onUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Annuler")
                }
                IconButton(onClick = onDice) {
                    Icon(Icons.Filled.Casino, contentDescription = "Dés")
                }
                IconButton(onClick = onLog) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Journal")
                }
                IconButton(onClick = onNewGame) {
                    Icon(Icons.Filled.Add, contentDescription = "Nouvelle partie")
                }
                IconButton(onClick = { showEndConfirm = true }) {
                    Icon(Icons.Filled.Flag, contentDescription = "Terminer")
                }
            }
        }
    }

    if (showEndConfirm) {
        SorceryDialog(
            title = "Terminer la partie ?",
            confirmText = "Terminer",
            onConfirm = {
                showEndConfirm = false
                onEndGame()
            },
            dismissText = "Annuler",
            onDismiss = { showEndConfirm = false },
        ) {
            Text("La partie sera enregistrée dans l'historique.")
        }
    }
}
