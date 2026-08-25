package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig

/** Dialogue de configuration d'une nouvelle partie (vie de départ). */
@Composable
fun NewGameDialog(
    onConfirm: (GameConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var startingLife by remember { mutableIntStateOf(GameConfig().startingLife) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle partie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vie de départ", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CounterStepper(
                        value = startingLife,
                        onValueChange = { startingLife = it },
                        min = 1,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(GameConfig(startingLife = startingLife)) }) {
                Text("Démarrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}
