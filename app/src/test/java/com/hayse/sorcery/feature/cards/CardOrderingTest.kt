package com.hayse.sorcery.feature.cards

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.ElementGroup
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.core.shared.model.elementGroupOf
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.GridRow
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.cards.domain.model.buildGridRows
import com.hayse.sorcery.feature.cards.domain.model.sortTypesForFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardOrderingTest {

    private fun card(
        name: String,
        type: String = "Minion",
        rarity: Rarity? = Rarity.Ordinary,
        elements: List<Element> = emptyList(),
    ): Card = Card(
        name = name,
        type = type,
        rarity = rarity,
        rulesText = "",
        cost = null,
        attack = null,
        defence = null,
        life = null,
        thresholds = emptyMap(),
        elements = elements,
        subTypes = emptyList(),
        imageUri = null,
    )

    private fun entry(setName: String, card: Card) = SetEntry(setName, card, card)

    private fun cells(rows: List<GridRow<Card>>): List<String> =
        rows.filterIsInstance<GridRow.Cell<Card>>().map { it.card.name }

    @Test
    fun `elementGroupOf classe neutre multi et mono-element`() {
        assertEquals(ElementGroup.Neutral, elementGroupOf(emptyList()))
        assertEquals(ElementGroup.Fire, elementGroupOf(listOf(Element.Fire)))
        assertEquals(ElementGroup.Multi, elementGroupOf(listOf(Element.Fire, Element.Water)))
    }

    @Test
    fun `sortTypesForFilter respecte l'ordre Site Magic Aura Artifact Minion`() {
        val sorted = sortTypesForFilter(listOf("Minion", "Aura", "Site", "Artifact", "Magic"))
        assertEquals(listOf("Site", "Magic", "Aura", "Artifact", "Minion"), sorted)
    }

    @Test
    fun `sortTypesForFilter met les types inconnus en dernier tries par nom`() {
        val sorted = sortTypesForFilter(listOf("Zeta", "Site", "Alpha"))
        assertEquals(listOf("Site", "Alpha", "Zeta"), sorted)
    }

    @Test
    fun `buildGridRows insere un en-tete par set dans l'ordre canonique`() {
        val rows = buildGridRows(
            listOf(
                entry("Gothic", card("g")),
                entry("Alpha", card("a")),
                entry("Beta", card("b")),
            ),
        )
        val headers = rows.filterIsInstance<GridRow.SetHeader>().map { it.setName }
        assertEquals(listOf("Alpha", "Beta", "Gothic"), headers)
    }

    @Test
    fun `buildGridRows trie Avatar puis element type rarete dans un set`() {
        val avatar = card("Avatar", type = "Avatar", elements = listOf(Element.Fire))
        val neutralSite = card("N Site", type = "Site")
        val fireMinion = card("F Minion", type = "Minion", elements = listOf(Element.Fire))
        val fireArtifact = card("F Artifact", type = "Artifact", elements = listOf(Element.Fire))
        val rows = buildGridRows(
            listOf(
                entry("Alpha", fireMinion),
                entry("Alpha", neutralSite),
                entry("Alpha", avatar),
                entry("Alpha", fireArtifact),
            ),
        )
        assertEquals(listOf("Avatar", "N Site", "F Artifact", "F Minion"), cells(rows))
    }

    @Test
    fun `buildGridRows trie par rarete Unique avant Ordinary`() {
        val rows = buildGridRows(
            listOf(
                entry("Alpha", card("ord", type = "Minion", rarity = Rarity.Ordinary)),
                entry("Alpha", card("uniq", type = "Minion", rarity = Rarity.Unique)),
                entry("Alpha", card("elite", type = "Minion", rarity = Rarity.Elite)),
            ),
        )
        assertEquals(listOf("uniq", "elite", "ord"), cells(rows))
    }

    @Test
    fun `buildGridRows ajoute un separateur a chaque changement de groupe`() {
        val rows = buildGridRows(
            listOf(
                entry("Alpha", card("f1", type = "Minion", elements = listOf(Element.Fire))),
                entry("Alpha", card("f2", type = "Minion", elements = listOf(Element.Fire))),
                entry("Alpha", card("w1", type = "Minion", elements = listOf(Element.Water))),
            ),
        )
        val labels = rows.filterIsInstance<GridRow.GroupLabel>().map { it.text }
        assertEquals(listOf("Fire · Minion", "Water · Minion"), labels)
    }

    @Test
    fun `buildGridRows commence chaque set par un en-tete puis un separateur`() {
        val rows = buildGridRows(listOf(entry("Alpha", card("a", type = "Site"))))
        assertTrue(rows[0] is GridRow.SetHeader)
        assertTrue(rows[1] is GridRow.GroupLabel)
        assertTrue(rows[2] is GridRow.Cell)
    }
}
