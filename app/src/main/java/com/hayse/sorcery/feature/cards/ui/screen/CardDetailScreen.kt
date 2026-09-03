package com.hayse.sorcery.feature.cards.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.ui.LocalCardListContext
import com.hayse.sorcery.core.ui.composable.CardImage
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.core.ui.composable.ElementIcon
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.composable.LoadingState
import com.hayse.sorcery.core.ui.theme.LocalSetSkin
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.sorcerySetFromName
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.skin.SkinnedBackground
import com.hayse.sorcery.core.ui.theme.skin.SkinnedSectionTitle
import com.hayse.sorcery.core.ui.theme.skin.Skins
import com.hayse.sorcery.core.ui.theme.skin.skinFor
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.cards.domain.model.Printing
import com.hayse.sorcery.feature.cards.ui.viewmodel.CardDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CardDetailScreen(
    name: String,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = koinViewModel(key = name) { parametersOf(name) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> LoadingState(modifier)
        state.detail == null -> EmptyState(title = stringResource(R.string.cards_detail_not_found), modifier = modifier)
        else -> DetailContent(
            detail = state.detail!!,
            owned = state.owned,
            onAdjust = viewModel::adjust,
            modifier = modifier,
        )
    }
}

/**
 * Détail de carte avec swipe précédent/suivant : parcourt la liste ordonnée publiée par
 * l'écran d'origine ([LocalCardListContext]). Si cette carte n'y figure pas (ou liste vide),
 * on retombe sur l'affichage d'une seule carte.
 */
@Composable
fun CardDetailPager(name: String, modifier: Modifier = Modifier) {
    val context = LocalCardListContext.current
    val names = remember { context.names }
    val startIndex = remember(names, name) { names.indexOf(name) }
    if (startIndex < 0) {
        CardDetailScreen(name = name, modifier = modifier)
        return
    }
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { names.size })
    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        CardDetailScreen(name = names[page])
    }
}

@Composable
private fun DetailContent(
    detail: CardDetail,
    owned: Map<Pair<String, String>, Int>,
    onAdjust: (slug: String, finish: String, delta: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = detail.card
    val spacing = LocalSpacing.current
    val set = sorcerySetFromName(detail.printings.firstOrNull()?.setName)
    // On garde la décoration désactivée si le skin ambiant est neutre (mode Off).
    val skin = if (LocalSetSkin.current == Skins.Default) Skins.Default else skinFor(set)
    SorceryTheme(set = set, skin = skin) {
    SkinnedBackground {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        CardImage(
            // card.imageUri est résolu vers la 1re impression dont le .webp existe vraiment ;
            // l'URI brute d'une impression peut pointer vers un visuel absent (promo sans image).
            imageUri = card.imageUri ?: detail.printings.firstOrNull()?.imageUri,
            contentDescription = card.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.CenterHorizontally),
        )
        SkinnedSectionTitle(
            text = card.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        val subtitle = buildString {
            append(card.type)
            if (card.subTypes.isNotEmpty()) append(" · ${card.subTypes.joinToString(", ")}")
            card.rarity?.let { append(" · ${it.name}") }
        }
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        StatsRow(card)
        ThresholdsRow(card.thresholds)

        if (card.rulesText.isNotBlank()) {
            HorizontalDivider()
            Text(card.rulesText, style = MaterialTheme.typography.bodyLarge)
        }

        HorizontalDivider()
        SkinnedSectionTitle(
            text = stringResource(R.string.cards_printings),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        detail.printings.forEach { printing ->
            PrintingRow(
                printing = printing,
                quantity = owned[printing.slug to printing.finish] ?: 0,
                onAdjust = { delta -> onAdjust(printing.slug, printing.finish, delta) },
            )
        }
    }
    }
    }
}

@Composable
private fun StatsRow(card: Card) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        card.cost?.let { Stat(stringResource(R.string.cards_stat_cost), it) }
        card.attack?.let { Stat(stringResource(R.string.cards_stat_attack), it) }
        card.defence?.let { Stat(stringResource(R.string.cards_stat_defence), it) }
        card.life?.let { Stat(stringResource(R.string.cards_stat_life), it) }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ThresholdsRow(thresholds: Map<Element, Int>) {
    val active = thresholds.filterValues { it > 0 }
    if (active.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.cards_thresholds), style = MaterialTheme.typography.labelLarge)
        active.forEach { (element, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                ElementIcon(element = element, size = 18.dp)
                Text(" $value", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PrintingRow(printing: Printing, quantity: Int, onAdjust: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${printing.setName} · ${printing.finish} · ${printing.product}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (printing.artist.isNotBlank()) {
                Text(
                    text = stringResource(R.string.cards_artist, printing.artist),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        CounterStepper(
            value = quantity,
            onValueChange = { newValue -> onAdjust(newValue - quantity) },
            min = 0,
        )
    }
}
