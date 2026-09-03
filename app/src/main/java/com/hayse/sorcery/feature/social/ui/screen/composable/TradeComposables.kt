package com.hayse.sorcery.feature.social.ui.screen.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.composable.AdjustButton
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.social.domain.model.TradeableItem
import com.hayse.sorcery.feature.social.domain.model.WantedItem

/** Stepper compact borné [min]..[max], pour les tuiles de la grille. */
@Composable
private fun CompactStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AdjustButton(
            symbol = "−",
            contentDescription = stringResource(R.string.core_counter_decrement),
            onClick = { onValueChange((value - 1).coerceIn(min, max)) },
            onLongClick = { onValueChange((value - 5).coerceIn(min, max)) },
            size = 32.dp,
        )
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        AdjustButton(
            symbol = "+",
            contentDescription = stringResource(R.string.core_counter_increment),
            onClick = { onValueChange((value + 1).coerceIn(min, max)) },
            onLongClick = { onValueChange((value + 5).coerceIn(min, max)) },
            size = 32.dp,
        )
    }
}

/** Pied de tuile « À échanger » : finish + possédées + stepper plafonné à la quantité possédée. */
@Composable
fun TradeableFooter(
    item: TradeableItem,
    onSet: (slug: String, finish: String, quantity: Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.social_owned_count, item.printing.finish, item.ownedQty),
            style = MaterialTheme.typography.labelSmall,
        )
        CompactStepper(
            value = item.tradeableQty,
            onValueChange = { onSet(item.printing.slug, item.printing.finish, it) },
            min = 0,
            max = item.ownedQty,
        )
    }
}

/** Pied de tuile « Recherché » : finish + stepper (min 0 = suppression). */
@Composable
fun WantedFooter(
    item: WantedItem,
    onSet: (slug: String, finish: String, quantity: Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = item.printing.finish, style = MaterialTheme.typography.labelSmall)
        CompactStepper(
            value = item.wantedQty,
            onValueChange = { onSet(item.printing.slug, item.printing.finish, it) },
            min = 0,
            max = 99,
        )
    }
}

/** Contenu de la feuille « Ajouter à la liste recherchée » : recherche → carte → impression. */
@Composable
fun AddWantedSheetContent(
    query: String,
    results: List<com.hayse.sorcery.feature.cards.domain.model.Card>,
    selection: CardDetail?,
    onQueryChange: (String) -> Unit,
    onSelectCard: (String) -> Unit,
    onClearSelection: () -> Unit,
    onPickPrinting: (slug: String, finish: String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.social_add_wanted_title),
            style = MaterialTheme.typography.titleMedium,
        )
        if (selection == null) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                label = { Text(stringResource(R.string.social_add_wanted_search)) },
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(results, key = { it.name }) { card ->
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCard(card.name) }
                            .padding(vertical = 12.dp),
                    )
                    HorizontalDivider()
                }
            }
        } else {
            TextButton(onClick = onClearSelection) {
                Text(stringResource(R.string.social_add_wanted_back))
            }
            Text(selection.card.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(R.string.social_add_wanted_pick_printing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            selection.printings.forEach { printing ->
                SuggestionChip(
                    onClick = { onPickPrinting(printing.slug, printing.finish) },
                    label = { Text("${printing.setName} · ${printing.finish}") },
                )
            }
        }
    }
}
