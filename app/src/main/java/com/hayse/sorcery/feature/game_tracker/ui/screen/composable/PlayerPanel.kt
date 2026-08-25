package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerState

/** Panneau complet d'un joueur : vie, mana, sites, affinités. Le libellé indique le tour actif. */
@Composable
fun PlayerPanel(
    title: String,
    isActive: Boolean,
    player: PlayerState,
    onDamage: (Int) -> Unit,
    onLifeGain: (Int) -> Unit,
    onManaDelta: (Int) -> Unit,
    onSiteDelta: (Int) -> Unit,
    onAffinityDelta: (Element, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isActive) 4.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (isActive) "$title — tour actif" else title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            )
            LifeCounter(
                life = player.life,
                status = player.avatarStatus,
                onDamage = onDamage,
                onLifeGain = onLifeGain,
            )
            ResourceRow(label = "Mana", value = player.manaAvailable, onDelta = onManaDelta)
            ResourceRow(label = "Sites", value = player.sitesControlled, onDelta = onSiteDelta)
            AffinityRow(affinity = player.affinity, onDelta = onAffinityDelta)
        }
    }
}
