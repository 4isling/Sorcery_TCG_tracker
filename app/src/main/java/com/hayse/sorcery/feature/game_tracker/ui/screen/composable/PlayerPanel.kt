package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.ui.composable.CardImage
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
            val displayName = player.pseudo?.takeIf(String::isNotBlank) ?: title
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (player.avatarImageUri != null) {
                    CardImage(
                        imageUri = player.avatarImageUri,
                        contentDescription = player.avatarName,
                        modifier = Modifier
                            .size(width = 32.dp, height = 44.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isActive) "$displayName — tour actif" else displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    )
                    player.avatarName?.let {
                        Text(text = it, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
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
