package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.ui.composable.ElementIcon

/**
 * Ligne d'affinités : une pastille par élément avec son compteur.
 * Tap = +1, appui long = −1 (l'affinité est un seuil, pas une ressource dépensée).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AffinityRow(
    affinity: Map<Element, Int>,
    onDelta: (Element, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Element.entries.forEach { element ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onDelta(element, 1) },
                        onLongClick = { onDelta(element, -1) },
                    )
                    .padding(4.dp),
            ) {
                ElementIcon(element = element)
                Text(
                    text = (affinity[element] ?: 0).toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
