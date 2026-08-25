package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.ui.composable.CounterStepper

/** Ligne libellé + [CounterStepper] pour une ressource bornée à 0 (mana, sites). */
@Composable
fun ResourceRow(
    label: String,
    value: Int,
    onDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        CounterStepper(
            value = value,
            onValueChange = { onDelta(it - value) },
            min = 0,
        )
    }
}
