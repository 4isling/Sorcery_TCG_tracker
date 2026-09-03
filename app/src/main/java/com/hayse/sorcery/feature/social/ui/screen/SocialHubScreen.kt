package com.hayse.sorcery.feature.social.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing

private enum class SocialTab { Room, Lists, Saved }

/** Hub de l'onglet Échanges : room interactive, listes d'aide, échanges sauvegardés. */
@Composable
fun SocialHubScreen(
    onStartGlobal: () -> Unit,
    onStartRoom: () -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(SocialTab.Room) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = tab.ordinal) {
            SocialTab.entries.forEach { entry ->
                Tab(
                    selected = tab == entry,
                    onClick = { tab = entry },
                    text = { Text(socialTabLabel(entry)) },
                )
            }
        }

        when (tab) {
            SocialTab.Room -> RoomLauncher(onStartGlobal = onStartGlobal, onStartRoom = onStartRoom)
            SocialTab.Lists -> TradeScreen(onCardClick = onCardClick)
            SocialTab.Saved -> SavedTradesScreen()
        }
    }
}

@Composable
private fun RoomLauncher(onStartGlobal: () -> Unit, onStartRoom: () -> Unit) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.fillMaxSize().padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.social_choose_global),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onStartGlobal, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Public, contentDescription = null)
            Text(
                text = stringResource(R.string.social_choose_global),
                modifier = Modifier.padding(start = spacing.sm),
            )
        }
        Text(
            text = stringResource(R.string.room_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onStartRoom, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null)
            Text(
                text = stringResource(R.string.social_choose_room),
                modifier = Modifier.padding(start = spacing.sm),
            )
        }
    }
}

@Composable
private fun socialTabLabel(tab: SocialTab): String = when (tab) {
    SocialTab.Room -> stringResource(R.string.social_tab_room)
    SocialTab.Lists -> stringResource(R.string.social_tab_lists)
    SocialTab.Saved -> stringResource(R.string.social_tab_saved)
}
