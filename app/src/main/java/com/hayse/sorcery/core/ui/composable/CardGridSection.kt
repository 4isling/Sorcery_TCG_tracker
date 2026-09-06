package com.hayse.sorcery.core.ui.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** En-tête pleine largeur d'un set dans la grille de cartes. */
@Composable
fun SetHeaderRow(setName: String, modifier: Modifier = Modifier) {
    Text(
        text = setName,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
    )
}

/** Séparateur pleine largeur marquant un groupe (élément · type) sous un set. */
@Composable
fun GroupLabelRow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 8.dp),
    )
}
