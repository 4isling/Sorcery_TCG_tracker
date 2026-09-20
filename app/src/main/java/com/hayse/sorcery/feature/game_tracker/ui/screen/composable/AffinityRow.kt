package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.ui.composable.AdjustButton
import com.hayse.sorcery.core.ui.composable.ElementIcon

/**
 * Ligne d'affinités : une pastille par élément avec ses boutons − / + explicites.
 * L'affinité est un seuil (pas une ressource dépensée), et peut monter comme descendre.
 */
@Composable
fun AffinityRow(
    affinity: Map<Element, Int>,
    onDelta: (Element, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Element.entries.forEach { element ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ElementIcon(element = element)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    AdjustButton(
                        symbol = "−",
                        contentDescription = stringResource(R.string.game_affinity_decrease, element.name),
                        onClick = { onDelta(element, -1) },
                        onLongClick = { onDelta(element, -5) },
                        size = 24.dp,
                        symbolStyle = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = (affinity[element] ?: 0).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 12.dp),
                    )
                    AdjustButton(
                        symbol = "+",
                        contentDescription = stringResource(R.string.game_affinity_increase, element.name),
                        onClick = { onDelta(element, 1) },
                        onLongClick = { onDelta(element, 5) },
                        size = 24.dp,
                        symbolStyle = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}