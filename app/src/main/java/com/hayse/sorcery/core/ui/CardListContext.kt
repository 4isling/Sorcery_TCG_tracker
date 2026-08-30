package com.hayse.sorcery.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Ordre courant des cartes affichées par l'écran visible (browser, collection, deck…).
 *
 * Fourni au-dessus du NavHost, il survit à la navigation : quand on ouvre le détail d'une
 * carte, celui-ci lit la dernière liste publiée pour permettre le swipe précédent/suivant.
 */
class CardListContext {
    var names by mutableStateOf<List<String>>(emptyList())
}

val LocalCardListContext = staticCompositionLocalOf { CardListContext() }

/** Publie la liste ordonnée des cartes de l'écran courant tant que cet appel est composé. */
@Composable
fun ProvideCardList(names: List<String>) {
    val context = LocalCardListContext.current
    LaunchedEffect(names) { context.names = names }
}
