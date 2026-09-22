package com.hayse.sorcery.feature.statistics.data.repository

import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import com.hayse.sorcery.feature.statistics.domain.model.CountBucket
import com.hayse.sorcery.feature.statistics.domain.model.GameStats
import com.hayse.sorcery.feature.statistics.domain.model.LabelCount

/** Logique pure des statistiques de parties, testable en JVM sans dépendance Android. */
object GameStatsComputations {

    private const val TOP_AVATARS = 6

    fun compute(games: List<GameRecord>): GameStats {
        if (games.isEmpty()) return GameStats()

        val playerOneWins = games.count { it.winner == PlayerId.One }
        val playerTwoWins = games.count { it.winner == PlayerId.Two }
        val draws = games.count { it.winner == null }
        val averageTurns = games.map { it.turns }.average()
        val durations = games.mapNotNull { it.durationSeconds }
        val averageDuration = if (durations.isEmpty()) 0.0 else durations.average()
        val perTurn = games.mapNotNull { game ->
            game.durationSeconds?.takeIf { game.turns > 0 }?.let { it.toDouble() / game.turns }
        }
        val averageSecondsPerTurn = if (perTurn.isEmpty()) 0.0 else perTurn.average()

        val avatars = HashMap<String, Int>()
        for (game in games) {
            game.playerOneAvatar?.takeIf { it.isNotBlank() }?.let { avatars[it] = (avatars[it] ?: 0) + 1 }
            game.playerTwoAvatar?.takeIf { it.isNotBlank() }?.let { avatars[it] = (avatars[it] ?: 0) + 1 }
        }
        val topAvatars = avatars.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase() })
            .take(TOP_AVATARS)
            .map { LabelCount(it.key, it.value) }

        return GameStats(
            totalGames = games.size,
            playerOneWins = playerOneWins,
            playerTwoWins = playerTwoWins,
            draws = draws,
            averageTurns = averageTurns,
            averageDurationSeconds = averageDuration,
            averageSecondsPerTurn = averageSecondsPerTurn,
            turnBuckets = turnBuckets(games),
            topAvatars = topAvatars,
        )
    }

    private val BUCKET_BOUNDS = listOf(5, 10, 15, 20)

    private fun turnBuckets(games: List<GameRecord>): List<CountBucket> {
        val labels = listOf("1–5", "6–10", "11–15", "16–20", "21+")
        val counts = IntArray(labels.size)
        for (game in games) {
            val idx = BUCKET_BOUNDS.indexOfFirst { game.turns <= it }.let { if (it >= 0) it else labels.lastIndex }
            counts[idx]++
        }
        return labels.mapIndexed { i, label -> CountBucket(label, counts[i]) }
    }
}
