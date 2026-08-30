package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.feature.game_tracker.domain.model.DiceRollPurpose
import com.hayse.sorcery.feature.game_tracker.domain.model.GameEvent
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState

private val PRESETS = listOf(2, 6, 10, 20)

/** Lanceur de dés : nb de dés × faces, tirage du premier joueur, et mode Harbinger (3d20). */
@Composable
fun DiceRollerDialog(
    game: GameState,
    onRollDice: (count: Int, faces: Int) -> Unit,
    onRollFirstPlayer: () -> Unit,
    onRollHarbinger: () -> Unit,
    onDismiss: () -> Unit,
) {
    var count by remember { mutableIntStateOf(1) }
    var faces by remember { mutableIntStateOf(6) }
    val harbingerAvailable = game.players.values.any { it.avatarName == "Harbinger" }
    val lastRoll = game.history.lastOrNull { it is GameEvent.DiceRoll || it is GameEvent.FirstPlayerRoll }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lancer de dés") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nombre de dés", style = MaterialTheme.typography.titleSmall)
                CounterStepper(value = count, onValueChange = { count = it }, min = 1, max = 20)

                Text("Faces par dé", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PRESETS.forEach { preset ->
                        FilterChip(
                            selected = faces == preset,
                            onClick = { faces = preset },
                            label = { Text("d$preset") },
                        )
                    }
                }
                CounterStepper(value = faces, onValueChange = { faces = it }, min = 2, max = 100)

                Button(onClick = { onRollDice(count, faces) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Lancer ${count}d$faces")
                }
                OutlinedButton(onClick = onRollFirstPlayer, modifier = Modifier.fillMaxWidth()) {
                    Text("Déterminer le premier joueur")
                }
                if (harbingerAvailable) {
                    OutlinedButton(onClick = onRollHarbinger, modifier = Modifier.fillMaxWidth()) {
                        Text("Harbinger : lancer 3d20")
                    }
                }

                if (lastRoll != null) {
                    HorizontalDivider()
                    LastRollResult(lastRoll)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        },
    )
}

@Composable
private fun LastRollResult(event: GameEvent) {
    when (event) {
        is GameEvent.DiceRoll -> {
            val title = if (event.purpose == DiceRollPurpose.Harbinger) {
                "Harbinger (3d20)"
            } else {
                "${event.results.size}d${event.faces}"
            }
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = event.results.joinToString("  ") { it.toString() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (event.purpose != DiceRollPurpose.Harbinger && event.results.size > 1) {
                Text("Total : ${event.results.sum()}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        is GameEvent.FirstPlayerRoll -> {
            Text("Premier joueur", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Joueur ${if (event.chosen.name == "One") "1" else "2"} commence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        else -> Unit
    }
}
