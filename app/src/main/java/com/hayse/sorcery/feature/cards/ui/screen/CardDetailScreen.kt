package com.hayse.sorcery.feature.cards.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.ui.composable.CounterStepper
import com.hayse.sorcery.core.ui.composable.ElementIcon
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
    viewModel: CardDetailViewModel = koinViewModel { parametersOf(name) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> Box(modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        state.detail == null -> Box(modifier.fillMaxSize(), Alignment.Center) {
            Text("Carte introuvable", style = MaterialTheme.typography.bodyLarge)
        }
        else -> DetailContent(
            detail = state.detail!!,
            owned = state.owned,
            onAdjust = viewModel::adjust,
            modifier = modifier,
        )
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = detail.printings.firstOrNull()?.imageUri ?: card.imageUri,
            contentDescription = card.name,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.CenterHorizontally),
        )
        Text(card.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

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
        Text("Impressions", style = MaterialTheme.typography.titleMedium)
        detail.printings.forEach { printing ->
            PrintingRow(
                printing = printing,
                quantity = owned[printing.slug to printing.finish] ?: 0,
                onAdjust = { delta -> onAdjust(printing.slug, printing.finish, delta) },
            )
        }
    }
}

@Composable
private fun StatsRow(card: Card) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        card.cost?.let { Stat("Coût", it) }
        card.attack?.let { Stat("Att", it) }
        card.defence?.let { Stat("Déf", it) }
        card.life?.let { Stat("Vie", it) }
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
        Text("Seuils", style = MaterialTheme.typography.labelLarge)
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
                    text = "Illustration : ${printing.artist}",
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
