package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.ui.screen.composable.NewGameDialog
import com.hayse.sorcery.feature.game_tracker.ui.screen.composable.PlayerPanel
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameTrackerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameTrackerScreen(
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

    var showNewGameDialog by remember { mutableStateOf(false) }

    if (showNewGameDialog) {
        NewGameDialog(
            onConfirm = { config ->
                viewModel.newGame(config)
                showNewGameDialog = false
            },
            onDismiss = { showNewGameDialog = false },
        )
    }

    when {
        state.loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.game == null -> {
            EmptyState(modifier = modifier, onNewGame = { showNewGameDialog = true })
        }

        else -> {
            GameContent(
                game = state.game!!,
                viewModel = viewModel,
                onNewGame = { showNewGameDialog = true },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, onNewGame: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Adversaire (Two) : panneau retourné à 180° pour un vis-à-vis sur table.
        PlayerPanel(
            title = "Joueur 2",
            isActive = game.activePlayer == PlayerId.Two,
            player = game.player(PlayerId.Two),
            onDamage = { viewModel.damage(PlayerId.Two, it) },
            onLifeGain = { viewModel.lifeGain(PlayerId.Two, it) },
            onManaDelta = { viewModel.manaDelta(PlayerId.Two, it) },
            onSiteDelta = { viewModel.siteDelta(PlayerId.Two, it) },
            onAffinityDelta = { element, delta -> viewModel.affinityDelta(PlayerId.Two, element, delta) },
            modifier = Modifier.rotate(180f),
        )

        ControlBar(
            turn = game.turn,
            onNewTurn = viewModel::newTurn,
            onUndo = viewModel::undo,
            onNewGame = onNewGame,
            onEndGame = viewModel::endGame,
        )

        PlayerPanel(
            title = "Joueur 1",
            isActive = game.activePlayer == PlayerId.One,
            player = game.player(PlayerId.One),
            onDamage = { viewModel.damage(PlayerId.One, it) },
            onLifeGain = { viewModel.lifeGain(PlayerId.One, it) },
            onManaDelta = { viewModel.manaDelta(PlayerId.One, it) },
            onSiteDelta = { viewModel.siteDelta(PlayerId.One, it) },
            onAffinityDelta = { element, delta -> viewModel.affinityDelta(PlayerId.One, element, delta) },
        )
    }
}

@Composable
private fun ControlBar(
    turn: Int,
    onNewTurn: () -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    onEndGame: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Tour $turn", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewTurn) { Text("Tour suivant") }
            OutlinedButton(onClick = onUndo) { Text("Annuler") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onNewGame) { Text("Nouvelle") }
            OutlinedButton(onClick = onEndGame) { Text("Terminer") }
        }
    }
}
