package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.AdjustButton
import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus

/**
 * Compteur de vie de l'avatar. Le bouton descendant applique des DÉGÂTS (tap = 1, appui long = 5)
 * et non un simple décrément : c'est indispensable pour déclencher le death blow quand la vie est déjà à 0.
 */
@Composable
fun LifeCounter(
    life: Int,
    status: AvatarStatus,
    onDamage: (Int) -> Unit,
    onLifeGain: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AdjustButton(
                symbol = "−",
                contentDescription = stringResource(R.string.game_deal_damage),
                onClick = { onDamage(1) },
                onLongClick = { onDamage(5) },
                size = 64.dp,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = life.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            AdjustButton(
                symbol = "+",
                contentDescription = stringResource(R.string.game_gain_life),
                onClick = { onLifeGain(1) },
                onLongClick = { onLifeGain(5) },
                size = 64.dp,
            )
        }
        if (status != AvatarStatus.Active) {
            Text(
                text = when (status) {
                    AvatarStatus.DeathsDoor -> stringResource(R.string.game_status_deaths_door)
                    AvatarStatus.Defeated -> stringResource(R.string.game_status_defeated)
                    AvatarStatus.Active -> ""
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
