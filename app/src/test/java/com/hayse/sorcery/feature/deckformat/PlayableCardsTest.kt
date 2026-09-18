package com.hayse.sorcery.feature.deckformat

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.deckformat.domain.PlayableCards
import com.hayse.sorcery.feature.deckformat.domain.SorceryFormats
import com.hayse.sorcery.feature.deckformat.domain.model.PlayableCard
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayableCardsTest {

    private val poorcery = SorceryFormats.Poorcery

    private fun playable(owned: List<Pair<Card, Int>>) = PlayableCards.compute(owned, poorcery)

    @Test
    fun `owned is summed across printings and capped at the copy limit`() {
        val cave = card("Cave-In", "Magic", Rarity.Exceptional)
        val result = playable(listOf(cave to 2, cave to 3))
        assertEquals(listOf(PlayableCard("Cave-In", owned = 5, playable = 3)), result)
    }

    @Test
    fun `unlimited copy cards are fully playable`() {
        val wolves = card("Grey Wolves", "Minion", Rarity.Ordinary)
        val result = playable(listOf(wolves to 9))
        assertEquals(listOf(PlayableCard("Grey Wolves", owned = 9, playable = 9)), result)
    }

    @Test
    fun `cards of a banned rarity are excluded`() {
        val elite = card("Elite Guy", "Minion", Rarity.Elite)
        assertEquals(emptyList<PlayableCard>(), playable(listOf(elite to 3)))
    }

    @Test
    fun `banned cards are excluded`() {
        val format = poorcery.copy(bannedCards = setOf("Cave-In"))
        val cave = card("Cave-In", "Magic", Rarity.Exceptional)
        assertEquals(emptyList<PlayableCard>(), PlayableCards.compute(listOf(cave to 3), format))
    }

    @Test
    fun `avatars and tokens are excluded`() {
        val avatar = card("Sorcerer", "Avatar", null)
        val token = card("Foot Soldier", "Token", Rarity.Ordinary)
        assertEquals(emptyList<PlayableCard>(), playable(listOf(avatar to 1, token to 2)))
    }

    @Test
    fun `playable never exceeds owned`() {
        val bury = card("Bury", "Magic", Rarity.Ordinary)
        val result = playable(listOf(bury to 2))
        assertEquals(listOf(PlayableCard("Bury", owned = 2, playable = 2)), result)
    }
}
