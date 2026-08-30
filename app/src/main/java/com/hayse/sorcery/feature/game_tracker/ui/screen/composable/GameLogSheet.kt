package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.DiceRollPurpose
import com.hayse.sorcery.feature.game_tracker.domain.model.GameEvent
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId

/** Journal de la partie en cours (bottom sheet), du plus récent au plus ancien. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLogSheet(
    game: GameState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Journal de la partie", style = MaterialTheme.typography.titleLarge)
            if (game.history.isEmpty()) {
                Text("Aucune action pour l'instant.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val entries = game.history.reversed()
                    itemsIndexed(entries) { index, event ->
                        Text(
                            text = event.frenchLabel(game),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (index < entries.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun GameState.playerLabel(id: PlayerId): String {
    val pseudo = player(id).pseudo?.takeIf(String::isNotBlank)
    return pseudo ?: if (id == PlayerId.One) "Joueur 1" else "Joueur 2"
}

private fun Element.french(): String = when (this) {
    Element.Air -> "Air"
    Element.Earth -> "Terre"
    Element.Fire -> "Feu"
    Element.Water -> "Eau"
}

private fun GameEvent.frenchLabel(game: GameState): String = when (this) {
    is GameEvent.Damage -> "${game.playerLabel(player)} subit $amount dégât(s)"
    is GameEvent.LifeLoss -> "${game.playerLabel(player)} perd $amount vie"
    is GameEvent.LifeGain -> "${game.playerLabel(player)} gagne $amount vie"
    is GameEvent.ManaAdjust -> "${game.playerLabel(player)} : mana ${signed(delta)}"
    is GameEvent.SiteCountChange -> "${game.playerLabel(player)} : sites ${signed(delta)}"
    is GameEvent.AffinityChange -> "${game.playerLabel(player)} : affinité ${element.french()} ${signed(delta)}"
    GameEvent.NewTurn -> "Nouveau tour"
    is GameEvent.DiceRoll -> if (purpose == DiceRollPurpose.Harbinger) {
        "Harbinger 3d20 : ${results.joinToString(", ")}"
    } else {
        "Jet ${results.size}d$faces : ${results.joinToString(", ")} (total ${results.sum()})"
    }
    is GameEvent.FirstPlayerRoll -> "Premier joueur : ${game.playerLabel(chosen)}"
}

private fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"
