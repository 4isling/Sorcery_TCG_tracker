package com.hayse.sorcery.feature.game_tracker.ui.screen.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Durée de « roulement » d'un dé (valeurs qui défilent + rotation), avant qu'il ne se fige. */
private const val ROLL_MS = 700
/** Décalage entre deux dés consécutifs, pour un effet de cascade. */
private const val STAGGER_MS = 90L
/** Nombre de valeurs intermédiaires affichées pendant le roulement. */
private const val ROLL_STEPS = 10

/**
 * Rangée de dés dont les résultats sont déjà connus. Si [animate], chaque dé roule (rotation,
 * valeurs aléatoires qui défilent) puis se fige sur sa valeur avec un rebond ; [onSettled] est
 * appelé une fois le dernier dé posé. [rollKey] identifie le jet : un nouveau jet relance l'animation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedDiceRow(
    results: List<Int>,
    faces: Int,
    rollKey: Int,
    animate: Boolean,
    onSettled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(rollKey) {
        if (animate) delay(STAGGER_MS * (results.size - 1) + ROLL_MS)
        onSettled()
    }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        results.forEachIndexed { index, value ->
            AnimatedDie(value = value, faces = faces, rollKey = rollKey, index = index, animate = animate)
        }
    }
}

@Composable
private fun AnimatedDie(value: Int, faces: Int, rollKey: Int, index: Int, animate: Boolean) {
    var shown by remember(rollKey) { mutableIntStateOf(if (animate) Random.nextInt(1, faces + 1) else value) }
    val rotation = remember(rollKey) { Animatable(0f) }
    val scale = remember(rollKey) { Animatable(1f) }

    LaunchedEffect(rollKey) {
        if (!animate) return@LaunchedEffect
        delay(STAGGER_MS * index)
        launch { rotation.animateTo(720f, tween(ROLL_MS, easing = FastOutSlowInEasing)) }
        repeat(ROLL_STEPS) {
            shown = Random.nextInt(1, faces + 1)
            delay((ROLL_MS / ROLL_STEPS).toLong())
        }
        shown = value
        scale.snapTo(1.3f)
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = shown.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/** Contenu révélé une fois les dés posés (total, joueur désigné…), avec fondu + glissement. */
@Composable
fun DiceSettledReveal(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 2 },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

/** État « posé » d'un jet : vrai d'emblée si le jet n'est pas animé, sinon bascule via [AnimatedDiceRow]. */
@Composable
fun rememberDiceSettled(rollKey: Int, animate: Boolean): MutableState<Boolean> =
    remember(rollKey) { mutableStateOf(!animate) }
