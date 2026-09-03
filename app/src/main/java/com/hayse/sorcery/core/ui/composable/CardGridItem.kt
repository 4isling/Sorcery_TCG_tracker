package com.hayse.sorcery.core.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing

/** Tuile de carte réutilisable : image (ratio carte) + nom sous celle-ci, badge optionnel en coin. */
@Composable
fun CardGridItem(
    imageUri: String?,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: (@Composable BoxScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CardImage(
                imageUri = imageUri,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(MaterialTheme.shapes.small),
            )
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(LocalSpacing.current.xs),
                ) { badge() }
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = LocalSpacing.current.xs),
        )
        footer?.invoke(this)
    }
}

/** Pastille compacte affichant un compteur (quantité possédée, surplus, etc.). */
@Composable
fun CountBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
