package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.skin.SkinnedCard

@Composable
fun GameMenuScreen(
    onNewGame: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        MenuCard(
            title = stringResource(R.string.game_new_game),
            subtitle = stringResource(R.string.game_menu_new_subtitle),
            icon = Icons.Filled.Add,
            onClick = onNewGame,
        )
        MenuCard(
            title = stringResource(R.string.game_history),
            subtitle = stringResource(R.string.game_menu_history_subtitle),
            icon = Icons.Filled.History,
            onClick = onHistory,
        )
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    SkinnedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
