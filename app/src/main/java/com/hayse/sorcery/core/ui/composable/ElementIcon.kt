package com.hayse.sorcery.core.ui.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.ui.theme.color

/** Symbole alchimique de l'élément (triangle, barré ou non), tracé en noir et teinté à l'affichage. */
@DrawableRes
fun Element.iconRes(): Int = when (this) {
    Element.Air -> R.drawable.ic_element_air
    Element.Earth -> R.drawable.ic_element_earth
    Element.Fire -> R.drawable.ic_element_fire
    Element.Water -> R.drawable.ic_element_water
}

/** Icône d'un élément : son symbole dans la couleur de l'élément ([tint] pour forcer une autre teinte). */
@Composable
fun ElementIcon(
    element: Element,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = element.color(),
) {
    Icon(
        painter = painterResource(element.iconRes()),
        contentDescription = element.name,
        tint = tint,
        modifier = modifier.size(size),
    )
}
