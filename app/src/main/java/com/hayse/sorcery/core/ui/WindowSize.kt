package com.hayse.sorcery.core.ui

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.compositionLocalOf

/** Classe de largeur de la fenêtre, calculée une fois dans MainActivity et propagée à l'arbre. */
val LocalWindowWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }

/** Grand écran (tablette) : layouts multi-panneaux. `Compact` = téléphone, comportement historique. */
val WindowWidthSizeClass.isExpanded: Boolean
    get() = this == WindowWidthSizeClass.Expanded

/** Écran au moins moyen : on peut afficher un rail de navigation plutôt qu'un drawer modal. */
val WindowWidthSizeClass.isAtLeastMedium: Boolean
    get() = this != WindowWidthSizeClass.Compact
