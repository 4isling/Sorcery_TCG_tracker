package com.hayse.sorcery.feature.game_tracker

import com.hayse.sorcery.feature.game_tracker.data.local.model.GameStateData
import com.hayse.sorcery.feature.game_tracker.data.local.model.GameTimerData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toData
import com.hayse.sorcery.feature.game_tracker.data.local.model.toDomain
import com.hayse.sorcery.feature.game_tracker.domain.model.GameConfig
import com.hayse.sorcery.feature.game_tracker.domain.model.GameMode
import com.hayse.sorcery.feature.game_tracker.domain.model.GameState
import com.hayse.sorcery.feature.game_tracker.domain.model.GameTimer
import com.hayse.sorcery.feature.game_tracker.domain.model.TimerConfig
import com.hayse.sorcery.feature.game_tracker.domain.repository.GameTimerSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePersistenceTest {

    @Test
    fun `game mode survives a persistence round trip`() {
        val solo = GameState.initial(GameConfig(mode = GameMode.Solo))
        assertEquals(GameMode.Solo, solo.toData().toDomain().config.mode)
    }

    @Test
    fun `games persisted before the mode existed are duels`() {
        val legacy = GameState.initial(GameConfig()).toData().copy(mode = "???")
        assertEquals(GameMode.Duel, legacy.toDomain().config.mode)
        assertEquals(GameMode.Duel, GameStateData(20, legacy.players, 1, 0).toDomain().config.mode)
    }

    @Test
    fun `verdict acknowledgement survives a persistence round trip`() {
        val started = GameTimer.start(TimerConfig(enabled = true))
        assertFalse(started.verdictAcknowledged)
        val acknowledged = started.copy(verdictAcknowledged = true)
        val restored = GameTimerSnapshot(acknowledged, lastTickEpochMs = 42L).toData().toDomain()
        assertTrue(restored.state.verdictAcknowledged)
        assertEquals(42L, restored.lastTickEpochMs)
    }

    @Test
    fun `timers persisted before acknowledgement existed are not acknowledged`() {
        assertFalse(GameTimerData().toDomain().state.verdictAcknowledged)
    }
}
