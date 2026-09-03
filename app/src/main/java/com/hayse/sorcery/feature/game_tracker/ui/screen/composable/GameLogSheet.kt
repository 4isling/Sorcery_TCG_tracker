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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
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
            Text(stringResource(R.string.game_log_title), style = MaterialTheme.typography.titleLarge)
            if (game.history.isEmpty()) {
                Text(stringResource(R.string.game_log_empty), style = MaterialTheme.typography.bodyMedium)
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

@Composable
private fun GameState.playerLabel(id: PlayerId): String {
    val pseudo = player(id).pseudo?.takeIf(String::isNotBlank)
    return pseudo ?: if (id == PlayerId.One) {
        stringResource(R.string.game_player_one)
    } else {
        stringResource(R.string.game_player_two)
    }
}

@Composable
private fun Element.french(): String = when (this) {
    Element.Air -> stringResource(R.string.game_element_air)
    Element.Earth -> stringResource(R.string.game_element_earth)
    Element.Fire -> stringResource(R.string.game_element_fire)
    Element.Water -> stringResource(R.string.game_element_water)
}

@Composable
private fun GameEvent.frenchLabel(game: GameState): String = when (this) {
    is GameEvent.Damage -> stringResource(R.string.game_event_damage, game.playerLabel(player), amount)
    is GameEvent.LifeLoss -> stringResource(R.string.game_event_life_loss, game.playerLabel(player), amount)
    is GameEvent.LifeGain -> stringResource(R.string.game_event_life_gain, game.playerLabel(player), amount)
    is GameEvent.ManaAdjust -> stringResource(R.string.game_event_mana, game.playerLabel(player), signed(delta))
    is GameEvent.SiteCountChange -> stringResource(R.string.game_event_sites, game.playerLabel(player), signed(delta))
    is GameEvent.AffinityChange -> stringResource(
        R.string.game_event_affinity,
        game.playerLabel(player),
        element.french(),
        signed(delta),
    )
    GameEvent.NewTurn -> stringResource(R.string.game_event_new_turn)
    is GameEvent.DiceRoll -> if (purpose == DiceRollPurpose.Harbinger) {
        stringResource(R.string.game_event_harbinger_roll, results.joinToString(", "))
    } else {
        stringResource(
            R.string.game_event_dice_roll,
            results.size,
            faces,
            results.joinToString(", "),
            results.sum(),
        )
    }
    is GameEvent.FirstPlayerRoll -> stringResource(R.string.game_event_first_player, game.playerLabel(chosen))
}

private fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"
