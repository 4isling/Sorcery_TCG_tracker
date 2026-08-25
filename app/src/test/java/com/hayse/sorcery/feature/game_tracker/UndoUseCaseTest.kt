package com.hayse.sorcery.feature.game_tracker

import com.hayse.sorcery.feature.game_tracker.domain.model.AvatarStatus
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.game_tracker.domain.usecase.ApplyDamageUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.StartNewTurnUseCase
import com.hayse.sorcery.feature.game_tracker.domain.usecase.UndoLastEventUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class UndoUseCaseTest {

    private val damage = ApplyDamageUseCase()
    private val newTurn = StartNewTurnUseCase()
    private val undo = UndoLastEventUseCase()

    private fun state(life: Int = 20) = GameState.initial(GameConfig(startingLife = life))

    @Test
    fun `undo on empty history is a no-op`() {
        val s = state()
        assertEquals(s, undo(s))
    }

    @Test
    fun `undo reverses the last damage`() {
        val s0 = state(20)
        val s1 = damage(s0, PlayerId.One, 5)
        val undone = undo(s1)
        assertEquals(20, undone.player(PlayerId.One).life)
        assertEquals(emptyList<Any>(), undone.history)
    }

    @Test
    fun `undo restores deaths door transition`() {
        var s = state(3)
        s = damage(s, PlayerId.One, 3) // -> DeathsDoor, life 0
        val undone = undo(s)
        assertEquals(3, undone.player(PlayerId.One).life)
        assertEquals(AvatarStatus.Active, undone.player(PlayerId.One).avatarStatus)
    }

    @Test
    fun `undo a new turn restores turn active player and mana`() {
        val s0 = state()
        val s1 = newTurn(s0)
        assertEquals(2, s1.turn)
        assertEquals(PlayerId.Two, s1.activePlayer)

        val undone = undo(s1)
        assertEquals(1, undone.turn)
        assertEquals(PlayerId.One, undone.activePlayer)
    }

    @Test
    fun `undo only removes the most recent event`() {
        var s = state(20)
        s = damage(s, PlayerId.One, 5) // life 15
        s = damage(s, PlayerId.One, 4) // life 11
        val undone = undo(s)
        assertEquals(15, undone.player(PlayerId.One).life)
        assertEquals(1, undone.history.size)
    }
}
