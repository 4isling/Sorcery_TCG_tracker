package com.hayse.sorcery.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * État partagé de la recherche de la barre supérieure.
 *
 * Le champ garde son propre [query] pour un écho immédiat à la frappe (sinon Compose
 * « perd » des caractères le temps de l'aller-retour vers le ViewModel). [owner] identifie
 * l'écran émetteur pour qu'un écran sortant n'efface pas la recherche de l'écran entrant.
 */
class TopBarSearchState {
    var owner by mutableStateOf<Any?>(null)
    var query by mutableStateOf("")
    var placeholder by mutableStateOf("Rechercher")
    var onQueryChange: (String) -> Unit = {}

    /** Le champ est replié derrière une icône loupe tant qu'on ne l'a pas déployé. */
    var expanded by mutableStateOf(false)

    val visible: Boolean get() = owner != null
}

val LocalTopBarSearch = staticCompositionLocalOf { TopBarSearchState() }

/**
 * Affiche un champ de recherche dans la barre supérieure tant que cet appel est composé.
 * À l'inverse (changement d'onglet, navigation), la barre reprend son titre.
 */
@Composable
fun ProvideTopBarSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Rechercher",
) {
    val state = LocalTopBarSearch.current
    val owner = remember { Any() }
    SideEffect {
        state.owner = owner
        state.onQueryChange = onQueryChange
        state.placeholder = placeholder
    }
    // Le champ n'est composé qu'au montage de l'écran/onglet ; on initialise la requête à ce
    // moment-là seulement (le flux « remplir une condition » re-monte l'onglet Ajouter, donc le
    // changement programmatique de la requête est bien pris en compte). Ensuite, sens unique.
    DisposableEffect(Unit) {
        state.query = query
        // Une requête déjà présente (ex. flux « remplir une condition ») déploie le champ.
        state.expanded = query.isNotEmpty()
        onDispose {
            if (state.owner === owner) {
                state.owner = null
                state.query = ""
                state.onQueryChange = {}
                state.expanded = false
            }
        }
    }
}
