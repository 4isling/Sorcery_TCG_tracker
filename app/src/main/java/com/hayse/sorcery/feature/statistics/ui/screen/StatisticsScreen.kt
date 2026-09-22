package com.hayse.sorcery.feature.statistics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.statistics.domain.model.CollectionStats
import com.hayse.sorcery.feature.statistics.domain.model.GameStats
import com.hayse.sorcery.feature.statistics.domain.model.SetStat
import com.hayse.sorcery.feature.statistics.ui.chart.BarRow
import com.hayse.sorcery.feature.statistics.ui.chart.ChartLegend
import com.hayse.sorcery.feature.statistics.ui.chart.DonutChart
import com.hayse.sorcery.feature.statistics.ui.chart.DonutSlice
import com.hayse.sorcery.feature.statistics.ui.chart.chartColor
import com.hayse.sorcery.feature.statistics.ui.viewmodel.StatisticsTab
import com.hayse.sorcery.feature.statistics.ui.viewmodel.StatisticsViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = koinViewModel(),
) {
    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val games by viewModel.games.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(StatisticsTab.Collection) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = tab.ordinal) {
            StatisticsTab.entries.forEach { entry ->
                Tab(
                    selected = tab == entry,
                    onClick = { tab = entry },
                    text = { Text(stringResource(tabLabel(entry))) },
                )
            }
        }
        when (tab) {
            StatisticsTab.Collection -> CollectionStatsContent(collection)
            StatisticsTab.Games -> GameStatsContent(games)
        }
    }
}

private fun tabLabel(tab: StatisticsTab): Int = when (tab) {
    StatisticsTab.Collection -> R.string.stat_tab_collection
    StatisticsTab.Games -> R.string.stat_tab_games
}

@Composable
private fun CollectionStatsContent(stats: CollectionStats) {
    if (stats.totalCards == 0) {
        EmptyState(title = stringResource(R.string.stat_empty))
        return
    }
    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item { TotalsCard(stats) }
        if (stats.byElement.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.stat_section_elements)) }
            item { ElementDonutCard(stats) }
        }
        item { SectionTitle(stringResource(R.string.stat_section_sets)) }
        item { SetBarsCard(stats.bySet) }
    }
}

@Composable
private fun TotalsCard(stats: CollectionStats) {
    val spacing = LocalSpacing.current
    SectionCard {
        StatLine(stringResource(R.string.stat_total_cards), stats.totalCards.toString(), emphasize = true)
        StatLine(stringResource(R.string.stat_distinct_cards), stats.distinctCards.toString())
        if (stats.totalBoosters > 0.0) {
            HorizontalDivider(modifier = Modifier.padding(vertical = spacing.xs))
            StatLine(stringResource(R.string.stat_total_boosters), format1(stats.totalBoosters))
            StatLine(stringResource(R.string.stat_total_displays), format2(stats.totalDisplays))
        }
    }
}

@Composable
private fun ElementDonutCard(stats: CollectionStats) {
    val spacing = LocalSpacing.current
    val slices = buildList {
        stats.byElement.forEach { entry ->
            add(DonutSlice(stringResource(elementGroupLabel(entry.group)), entry.count.toFloat(), entry.group.chartColor()))
        }
    }
    val total = stats.totalCards
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            DonutChart(
                slices = slices,
                centerValue = total.toString(),
                centerLabel = stringResource(R.string.stat_donut_cards),
            )
            ChartLegend(
                slices = slices,
                valueOf = { slice ->
                    val pct = if (total > 0) (slice.value.toInt() * 100 / total) else 0
                    stringResource(R.string.stat_element_value, slice.value.toInt(), pct)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SetBarsCard(sets: List<SetStat>) {
    val spacing = LocalSpacing.current
    val max = (sets.maxOfOrNull { it.ownedCards } ?: 0).coerceAtLeast(1)
    SectionCard {
        sets.forEachIndexed { index, set ->
            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = spacing.xs))
            BarRow(
                label = set.setName,
                valueText = stringResource(R.string.stat_cards_count, set.ownedCards),
                fraction = set.ownedCards.toFloat() / max,
            )
            sealedLabel(set)?.let { sealed ->
                Text(
                    text = sealed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GameStatsContent(stats: GameStats) {
    if (stats.totalGames == 0) {
        EmptyState(title = stringResource(R.string.stat_games_empty))
        return
    }
    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item { GameTotalsCard(stats) }
        item { SectionTitle(stringResource(R.string.stat_section_results)) }
        item { ResultsDonutCard(stats) }
        if (stats.topAvatars.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.stat_section_avatars)) }
            item { AvatarBarsCard(stats) }
        }
        item { SectionTitle(stringResource(R.string.stat_section_turns)) }
        item { TurnBucketsCard(stats) }
    }
}

@Composable
private fun GameTotalsCard(stats: GameStats) {
    SectionCard {
        StatLine(stringResource(R.string.stat_total_games), stats.totalGames.toString(), emphasize = true)
        StatLine(stringResource(R.string.stat_average_turns), format1(stats.averageTurns))
        if (stats.averageDurationSeconds > 0.0) {
            val seconds = stats.averageDurationSeconds.toInt()
            StatLine(
                stringResource(R.string.stat_average_duration),
                stringResource(R.string.stat_duration_minutes, seconds / 60, seconds % 60),
            )
        }
        if (stats.averageSecondsPerTurn > 0.0) {
            val perTurn = stats.averageSecondsPerTurn.toInt()
            StatLine(
                stringResource(R.string.stat_average_turn_duration),
                stringResource(R.string.stat_duration_minutes, perTurn / 60, perTurn % 60),
            )
        }
    }
}

@Composable
private fun ResultsDonutCard(stats: GameStats) {
    val spacing = LocalSpacing.current
    val slices = listOf(
        DonutSlice(stringResource(R.string.stat_result_p1), stats.playerOneWins.toFloat(), MaterialTheme.colorScheme.primary),
        DonutSlice(stringResource(R.string.stat_result_p2), stats.playerTwoWins.toFloat(), MaterialTheme.colorScheme.secondary),
        DonutSlice(stringResource(R.string.stat_result_draw), stats.draws.toFloat(), MaterialTheme.colorScheme.onSurfaceVariant),
    )
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            DonutChart(
                slices = slices,
                centerValue = stats.totalGames.toString(),
                centerLabel = stringResource(R.string.stat_donut_games),
            )
            ChartLegend(
                slices = slices,
                valueOf = { it.value.toInt().toString() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AvatarBarsCard(stats: GameStats) {
    val spacing = LocalSpacing.current
    val max = (stats.topAvatars.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    SectionCard {
        stats.topAvatars.forEachIndexed { index, avatar ->
            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = spacing.xs))
            BarRow(
                label = avatar.label,
                valueText = avatar.count.toString(),
                fraction = avatar.count.toFloat() / max,
            )
        }
    }
}

@Composable
private fun TurnBucketsCard(stats: GameStats) {
    val spacing = LocalSpacing.current
    val max = (stats.turnBuckets.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    SectionCard {
        stats.turnBuckets.forEachIndexed { index, bucket ->
            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = spacing.xs))
            BarRow(
                label = bucket.label,
                valueText = bucket.count.toString(),
                fraction = bucket.count.toFloat() / max,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) { content() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun StatLine(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun sealedLabel(set: SetStat): String? = when {
    set.boosters != null && set.displays != null ->
        stringResource(R.string.stat_booster_display, format1(set.boosters), format2(set.displays))
    set.boosters != null -> stringResource(R.string.stat_booster_only, format1(set.boosters))
    set.boxes != null -> stringResource(R.string.stat_boxes, format1(set.boxes))
    else -> null
}

private fun elementGroupLabel(group: ElementGroup): Int = when (group) {
    ElementGroup.Neutral -> R.string.stat_element_neutral
    ElementGroup.Earth -> R.string.stat_element_earth
    ElementGroup.Fire -> R.string.stat_element_fire
    ElementGroup.Water -> R.string.stat_element_water
    ElementGroup.Air -> R.string.stat_element_air
    ElementGroup.Multi -> R.string.stat_element_multi
}

private fun format1(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

private fun format2(value: Double): String = String.format(Locale.getDefault(), "%.2f", value)
