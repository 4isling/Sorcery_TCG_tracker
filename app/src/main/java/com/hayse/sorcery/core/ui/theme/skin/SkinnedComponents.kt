package com.hayse.sorcery.core.ui.theme.skin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.core.ui.theme.LocalSetSkin
import kotlin.random.Random

/** Fond de l'app : couleur de fond du thème + motif procédural du skin dessiné derrière le contenu. */
@Composable
fun SkinnedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val skin = LocalSetSkin.current
    val onBackground = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (val motif = skin.motif) {
            is SkinMotif.Noise -> Canvas(Modifier.fillMaxSize()) { drawNoise(onBackground, motif.alpha) }
            is SkinMotif.Scales -> Canvas(Modifier.fillMaxSize()) { drawScales(motif.stroke, motif.alpha) }
            else -> Unit
        }
        content()
    }
}

private fun DrawScope.drawNoise(color: Color, alpha: Float) {
    val count = (size.width * size.height / 1600f).toInt().coerceIn(0, 6000)
    val random = Random(0)
    repeat(count) {
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height
        drawCircle(color = color, radius = 1f, center = Offset(x, y), alpha = alpha)
    }
}

private fun DrawScope.drawScales(stroke: Color, alpha: Float) {
    val radius = 24.dp.toPx()
    val step = radius
    val strokeWidth = Stroke(width = 1.dp.toPx())
    var row = 0
    var y = 0f
    while (y < size.height + radius) {
        val offsetX = if (row % 2 == 0) 0f else radius
        var x = -radius + offsetX
        while (x < size.width + radius) {
            drawArc(
                color = stroke,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x, y - radius),
                size = Size(radius * 2, radius * 2),
                alpha = alpha,
                style = strokeWidth,
            )
            x += radius * 2
        }
        y += step
        row++
    }
}

/** Barre supérieure skinnée : titre en police d'affichage + filet doré sous la barre (motif GoldFrame). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinnedTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    val skin = LocalSetSkin.current
    val frame = (skin.motif as? SkinMotif.GoldFrame)?.color
    Column(modifier) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = { actions() },
        )
        if (frame != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(frame),
            )
        }
    }
}

/** Carte skinnée : forme medium du skin + éventuel filet de bordure. */
@Composable
fun SkinnedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val skin = LocalSetSkin.current
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = skin.shapes.medium,
            border = skin.cardBorder,
        ) { content() }
    } else {
        Card(
            modifier = modifier,
            shape = skin.shapes.medium,
            border = skin.cardBorder,
        ) { content() }
    }
}

/** Titre de section : style headline (police du skin) ; 1re lettre colorée si motif DropCap. */
@Composable
fun SkinnedSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val skin = LocalSetSkin.current
    val dropCap = (skin.motif as? SkinMotif.DropCap)?.color
    if (dropCap != null && text.isNotEmpty()) {
        val annotated = buildAnnotatedString {
            withStyle(SpanStyle(color = dropCap)) { append(text.substring(0, 1)) }
            append(text.substring(1))
        }
        Text(text = annotated, style = style, color = color, modifier = modifier)
    } else {
        Text(text = text, style = style, color = color, modifier = modifier)
    }
}
