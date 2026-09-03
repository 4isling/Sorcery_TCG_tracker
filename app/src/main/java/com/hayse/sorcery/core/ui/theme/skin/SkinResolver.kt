package com.hayse.sorcery.core.ui.theme.skin

import com.hayse.sorcery.core.ui.theme.sorcerySetFromName

/** Résout un skin depuis un nom de set tel qu'il apparaît dans les cartes. */
fun interface SkinResolver {
    fun forSet(setName: String?): SetSkin
}

val DefaultSkinResolver = SkinResolver { setName -> skinFor(sorcerySetFromName(setName)) }
