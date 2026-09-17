package com.hayse.sorcery.core.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R

/**
 * Compteur réutilisable : tap = ±[step], appui long = ±[largeStep]. La valeur est bornée à [min]..[max].
 */
@Composable
fun CounterStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
    largeStep: Int = 5,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    buttonSize: Dp = 48.dp,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    symbolStyle: TextStyle = MaterialTheme.typography.titleLarge,
    buttonContainer: Color = MaterialTheme.colorScheme.secondaryContainer,
    buttonContent: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    fun apply(delta: Int) = onValueChange((value + delta).coerceIn(min, max))

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AdjustButton(
            symbol = "−",
            contentDescription = stringResource(R.string.core_counter_decrement),
            onClick = { apply(-step) },
            onLongClick = { apply(-largeStep) },
            size = buttonSize,
            symbolStyle = symbolStyle,
            container = buttonContainer,
            content = buttonContent,
        )
        Text(
            text = value.toString(),
            style = valueStyle,
        )
        AdjustButton(
            symbol = "+",
            contentDescription = stringResource(R.string.core_counter_increment),
            onClick = { apply(step) },
            onLongClick = { apply(largeStep) },
            size = buttonSize,
            symbolStyle = symbolStyle,
            container = buttonContainer,
            content = buttonContent,
        )
    }
}
