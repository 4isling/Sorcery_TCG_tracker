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
 * Permet à un écran d'injecter ses propres actions dans la barre supérieure partagée, tant qu'il est
 * composé. [owner] identifie l'écran émetteur pour qu'un écran sortant n'efface pas les actions de
 * l'écran entrant.
 */
class TopBarActionsState {
    var owner by mutableStateOf<Any?>(null)
    var content by mutableStateOf<(@Composable () -> Unit)?>(null)
}

val LocalTopBarActions = staticCompositionLocalOf { TopBarActionsState() }

/**
 * Rendu des actions fournies par l'écran courant. Placé dans le slot `actions` de la barre ; la
 * disposition horizontale est assurée par le `TopAppBar` sous-jacent.
 */
@Composable
fun TopBarActionsSlot() {
    LocalTopBarActions.current.content?.invoke()
}

/**
 * Publie [content] dans la barre supérieure tant que cet appel est composé ; à la sortie de l'écran,
 * la barre reprend son état par défaut.
 */
@Composable
fun ProvideTopBarActions(content: @Composable () -> Unit) {
    val state = LocalTopBarActions.current
    val owner = remember { Any() }
    SideEffect {
        state.owner = owner
        state.content = content
    }
    DisposableEffect(Unit) {
        onDispose {
            if (state.owner === owner) {
                state.owner = null
                state.content = null
            }
        }
    }
}
