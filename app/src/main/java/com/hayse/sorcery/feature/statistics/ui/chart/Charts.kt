package com.hayse.sorcery.feature.statistics.ui.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.ui.theme.ElementAir
import com.hayse.sorcery.core.ui.theme.ElementEarth
import com.hayse.sorcery.core.ui.theme.ElementFire
import com.hayse.sorcery.core.ui.theme.ElementWater

/** Une part de graphique en anneau. */
data class DonutSlice(val label: String, val value: Float, val color: Color)

/**
 * Graphique en anneau (donut) tracé au [Canvas]. Les parts nulles sont ignorées.
 * [centerValue]/[centerLabel] s'affichent au centre. Rendu neutre si la somme est nulle.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerValue: String,
    centerLabel: String,
    modifier: Modifier = Modifier,
    ringWidth: Float = 46f,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = modifier.size(168.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().height(168.dp)) {
            val stroke = Stroke(width = ringWidth)
            val inset = ringWidth / 2f
            val arcSize = Size(size.width - ringWidth, size.height - ringWidth)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            if (total > 0f) {
                var start = -90f
                for (slice in slices) {
                    if (slice.value <= 0f) continue
                    val sweep = slice.value / total * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = centerValue, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Légende : pastille de couleur + libellé + valeur, une ligne par entrée non nulle. */
@Composable
fun ChartLegend(
    slices: List<DonutSlice>,
    valueOf: @Composable (DonutSlice) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        slices.filter { it.value > 0f }.forEach { slice ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(slice.color))
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(text = valueOf(slice), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * Barre horizontale proportionnelle animée : libellé au-dessus (avec valeur à droite),
 * piste arrondie et remplissage coloré à hauteur de [fraction] (0..1).
 */
@Composable
fun BarRow(
    label: String,
    valueText: String,
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        label = "bar",
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color),
            )
        }
    }
}

/** Couleur d'un groupe d'élément pour les graphiques. */
fun ElementGroup.chartColor(): Color = when (this) {
    ElementGroup.Neutral -> Color(0xFF9E9E9E)
    ElementGroup.Earth -> ElementEarth
    ElementGroup.Fire -> ElementFire
    ElementGroup.Water -> ElementWater
    ElementGroup.Air -> ElementAir
    ElementGroup.Multi -> Color(0xFF8E7CC3)
}
