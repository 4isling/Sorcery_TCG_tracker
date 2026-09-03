package com.hayse.sorcery.core.ui.theme.skin

import org.junit.Assert.assertEquals
import org.junit.Test

class SkinResolverTest {

    private val resolver = DefaultSkinResolver

    @Test
    fun `chaque nom de set connu resout vers son skin`() {
        assertEquals(Skins.Beta, resolver.forSet("Alpha"))
        assertEquals(Skins.Beta, resolver.forSet("Beta"))
        assertEquals(Skins.Arthurian, resolver.forSet("Arthurian Legends"))
        assertEquals(Skins.Arthurian, resolver.forSet("Arthurian"))
        assertEquals(Skins.Gothic, resolver.forSet("Gothic"))
        assertEquals(Skins.Dragonlord, resolver.forSet("Dragonlord"))
        assertEquals(Skins.Dragonlord, resolver.forSet("Dragonlords"))
        assertEquals(Skins.Default, resolver.forSet("Promotional"))
    }

    @Test
    fun `noms insensibles a la casse et aux espaces`() {
        assertEquals(Skins.Gothic, resolver.forSet("  gothic  "))
        assertEquals(Skins.Arthurian, resolver.forSet("ARTHURIAN LEGENDS"))
    }

    @Test
    fun `null et set inconnu retombent sur Default`() {
        assertEquals(Skins.Default, resolver.forSet(null))
        assertEquals(Skins.Default, resolver.forSet("Inconnu"))
        assertEquals(Skins.Default, resolver.forSet(""))
    }
}
