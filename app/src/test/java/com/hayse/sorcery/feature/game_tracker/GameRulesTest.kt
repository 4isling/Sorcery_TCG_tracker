package com.hayse.sorcery.feature.game_tracker

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.usecase.AdjustManaUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyDamageUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyLifeGainUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyLifeLossUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.StartNewTurnUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UpdateAffinityUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UpdateSiteCountUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class GameRulesTest {

    private val damage = ApplyDamageUseCase()
    private val lifeLoss = ApplyLifeLossUseCase()
    private val lifeGain = ApplyLifeGainUseCase()
    private val mana = AdjustManaUseCase()
    private val sites = UpdateSiteCountUseCase()
    private val affinity = UpdateAffinityUseCase()
    private val newTurn = StartNewTurnUseCase()

    private fun state(life: Int = 20) = GameState.initial(GameConfig(startingLife = life))

    private fun GameState.p1() = player(PlayerId.One)

    @Test
    fun `damage reduces life while active`() {
        val result = damage(state(20), PlayerId.One, 7)
        assertEquals(13, result.p1().life)
        assertEquals(AvatarStatus.Active, result.p1().avatarStatus)
    }

    @Test
    fun `life reaching zero puts avatar at deaths door`() {
        val result = damage(state(5), PlayerId.One, 5)
        assertEquals(0, result.p1().life)
        assertEquals(AvatarStatus.DeathsDoor, result.p1().avatarStatus)
    }

    @Test
    fun `overkill damage while active only reaches deaths door not defeat`() {
        val result = damage(state(5), PlayerId.One, 99)
        assertEquals(0, result.p1().life)
        assertEquals(AvatarStatus.DeathsDoor, result.p1().avatarStatus)
    }

    @Test
    fun `any damage at deaths door is a death blow`() {
        val atDoor = damage(state(3), PlayerId.One, 3)
        val result = damage(atDoor, PlayerId.One, 1)
        assertEquals(AvatarStatus.Defeated, result.p1().avatarStatus)
    }

    @Test
    fun `life gain is a no-op at deaths door`() {
        val atDoor = damage(state(3), PlayerId.One, 3)
        val result = lifeGain(atDoor, PlayerId.One, 10)
        assertEquals(0, result.p1().life)
        assertEquals(AvatarStatus.DeathsDoor, result.p1().avatarStatus)
    }

    @Test
    fun `direct life loss is a no-op at deaths door`() {
        val atDoor = damage(state(3), PlayerId.One, 3)
        val result = lifeLoss(atDoor, PlayerId.One, 5)
        assertEquals(AvatarStatus.DeathsDoor, result.p1().avatarStatus)
    }

    @Test
    fun `life loss reaching zero puts avatar at deaths door`() {
        val result = lifeLoss(state(4), PlayerId.One, 4)
        assertEquals(0, result.p1().life)
        assertEquals(AvatarStatus.DeathsDoor, result.p1().avatarStatus)
    }

    @Test
    fun `life gain increments while active`() {
        val result = lifeGain(state(20), PlayerId.One, 3)
        assertEquals(23, result.p1().life)
    }

    @Test
    fun `new turn toggles active player increments turn and resets active mana to sites`() {
        var s = state()
        s = sites(s, PlayerId.Two, 3) // Two: 3 sites, +3 mana
        s = mana(s, PlayerId.Two, -3) // spend it back to 0
        assertEquals(0, s.player(PlayerId.Two).manaAvailable)

        s = newTurn(s)

        assertEquals(2, s.turn)
        assertEquals(PlayerId.Two, s.activePlayer)
        assertEquals(3, s.player(PlayerId.Two).manaAvailable)
    }

    @Test
    fun `gaining a site mid-turn adds mana to the pool`() {
        val result = sites(state(), PlayerId.One, 2)
        assertEquals(2, result.p1().sitesControlled)
        assertEquals(2, result.p1().manaAvailable)
    }

    @Test
    fun `losing a site does not remove already generated mana`() {
        var s = sites(state(), PlayerId.One, 3) // sites 3, mana 3
        s = sites(s, PlayerId.One, -1) // sites 2, mana stays 3
        assertEquals(2, s.p1().sitesControlled)
        assertEquals(3, s.p1().manaAvailable)
    }

    @Test
    fun `mana cannot go below zero`() {
        val result = mana(state(), PlayerId.One, -5)
        assertEquals(0, result.p1().manaAvailable)
    }

    @Test
    fun `affinity counter increments and clamps at zero`() {
        var s = affinity(state(), PlayerId.One, Element.Fire, 2)
        assertEquals(2, s.p1().affinity[Element.Fire])
        s = affinity(s, PlayerId.One, Element.Fire, -5)
        assertEquals(0, s.p1().affinity[Element.Fire])
    }
}
