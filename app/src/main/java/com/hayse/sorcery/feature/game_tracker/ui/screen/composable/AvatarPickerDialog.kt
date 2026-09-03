package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.CardGridItem
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerIdentity
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.AvatarPickerViewModel
import org.koin.androidx.compose.koinViewModel

/** Grille de sélection d'un avatar (cartes de type Avatar). */
@Composable
fun AvatarPickerDialog(
    onPick: (PlayerIdentity) -> Unit,
    onDismiss: () -> Unit,
    viewModel: AvatarPickerViewModel = koinViewModel(),
) {
    val avatars by viewModel.avatars.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.game_choose_avatar)) },
        text = {
            if (avatars.isEmpty()) {
                Box(Modifier.fillMaxWidth().heightIn(min = 120.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                ) {
                    items(avatars, key = { it.name }) { card ->
                        CardGridItem(
                            imageUri = card.imageUri,
                            name = card.name,
                            onClick = {
                                onPick(
                                    PlayerIdentity(
                                        avatarName = card.name,
                                        avatarImageUri = card.imageUri,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.game_cancel)) }
        },
    )
}
