package com.hayse.sorcery.core.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.dimensions.Spacing
import com.hayse.sorcery.core.ui.theme.skin.SetSkin
import com.hayse.sorcery.core.ui.theme.skin.Skins

/** Set qui teinte la sous-arborescence courante (null = thème de base). */
val LocalSorcerySet = staticCompositionLocalOf<SorcerySet?> { null }

/** Skin décoratif (police/formes/bordure/motif) de la sous-arborescence courante. */
val LocalSetSkin = staticCompositionLocalOf<SetSkin> { Skins.Default }

private const val ThemeTransitionMillis = 450

@Composable
fun SorceryTheme(
    set: SorcerySet? = null,
    skin: SetSkin = Skins.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = paletteFor(set)
    val target = if (darkTheme) palette.dark else palette.light
    val spec = tween<androidx.compose.ui.graphics.Color>(ThemeTransitionMillis)

    // On anime les tokens principaux pour une bascule douce entre sets.
    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "onPrimary")
    val secondary by animateColorAsState(target.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(target.onSecondary, spec, label = "onSecondary")
    val background by animateColorAsState(target.background, spec, label = "background")
    val surface by animateColorAsState(target.surface, spec, label = "surface")

    val colorScheme = target.copy(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        background = background,
        surface = surface,
    )

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSorcerySet provides set,
        LocalSetSkin provides skin,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = sorceryTypography(skin.displayFont),
            shapes = skin.shapes,
            content = content,
        )
    }
}
