package com.hayse.sorcery.feature.cards.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
            wanted = state.wanted,
            onAdjust = viewModel::adjust,
            onSetWanted = viewModel::setWanted,
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
    wanted: Map<Pair<String, String>, Int>,
    onAdjust: (slug: String, finish: String, delta: Int) -> Unit,
    onSetWanted: (slug: String, finish: String, quantity: Int) -> Unit,
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
        // card.imageUri est résolu vers la 1re impression dont le .webp existe vraiment ;
        // l'URI brute d'une impression peut pointer vers un visuel absent (promo sans image).
        val imageUri = card.imageUri ?: detail.printings.firstOrNull()?.imageUri
        var fullscreen by remember { mutableStateOf(false) }
        CardImage(
            imageUri = imageUri,
            contentDescription = card.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(0.72f)
                .align(Alignment.CenterHorizontally)
                .clickable { fullscreen = true },
        )
        if (fullscreen) {
            FullscreenCardImage(
                imageUri = imageUri,
                contentDescription = card.name,
                onDismiss = { fullscreen = false },
            )
        }
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
        // Groupé par set : trait épais coloré entre sets, trait fin encart entre versions d'un même set.
        detail.printings.groupBy { it.setName }.entries.forEachIndexed { setIndex, (setName, printings) ->
            if (setIndex > 0) {
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = spacing.sm),
                )
            }
            Text(
                text = setName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            printings.forEachIndexed { i, printing ->
                if (i > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = spacing.lg),
                    )
                }
                PrintingRow(
                    printing = printing,
                    quantity = owned[printing.slug to printing.finish] ?: 0,
                    wantedQuantity = wanted[printing.slug to printing.finish] ?: 0,
                    onAdjust = { delta -> onAdjust(printing.slug, printing.finish, delta) },
                    onSetWanted = { qty -> onSetWanted(printing.slug, printing.finish, qty) },
                )
            }
        }
    }
    }
    }
}

@Composable
private fun FullscreenCardImage(
    imageUri: String?,
    contentDescription: String?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() },
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2f
                            offset = Offset.Zero
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            CardImage(
                imageUri = imageUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
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
private fun PrintingRow(
    printing: Printing,
    quantity: Int,
    wantedQuantity: Int,
    onAdjust: (Int) -> Unit,
    onSetWanted: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = printingVersionLabel(printing),
                style = MaterialTheme.typography.titleMedium,
            )
            if (printing.artist.isNotBlank()) {
                Row {
                    Text(
                        text = stringResource(R.string.cards_artist_label) + " : ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = printing.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        StepperWithLabel(
            label = stringResource(R.string.cards_detail_owned_label),
            value = quantity,
            onValueChange = { newValue -> onAdjust(newValue - quantity) },
            accent = MaterialTheme.colorScheme.secondary,
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        StepperWithLabel(
            label = stringResource(R.string.cards_detail_wanted_label),
            value = wantedQuantity,
            onValueChange = onSetWanted,
            accent = MaterialTheme.colorScheme.tertiary,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

/** Libellé de version : on omet le tirage de base « Booster » (c'est la version standard). */
private fun printingVersionLabel(printing: Printing): String {
    val parts = mutableListOf(printing.finish)
    if (printing.product != "Booster") parts += printing.product.replace('_', ' ')
    return parts.joinToString(" · ")
}

@Composable
private fun StepperWithLabel(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    accent: Color,
    container: Color,
    content: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
        CounterStepper(
            value = value,
            onValueChange = onValueChange,
            min = 0,
            buttonSize = 32.dp,
            valueStyle = MaterialTheme.typography.titleMedium,
            symbolStyle = MaterialTheme.typography.titleMedium,
            buttonContainer = container,
            buttonContent = content,
        )
    }
}
