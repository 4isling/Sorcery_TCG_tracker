package com.hayse.sorcery.feature.statistics.domain.model

/** Libellé + décompte (avatars joués, etc.). */
data class LabelCount(val label: String, val count: Int)

/** Tranche d'un histogramme (nombre de tours par exemple). */
data class CountBucket(val label: String, val count: Int)

/** Agrégat des statistiques issues de l'historique des parties. */
data class GameStats(
    val totalGames: Int = 0,
    val playerOneWins: Int = 0,
    val playerTwoWins: Int = 0,
    val draws: Int = 0,
    val averageTurns: Double = 0.0,
    /** Durée moyenne en secondes des parties chronométrées ; 0 si aucune n'a de durée connue. */
    val averageDurationSeconds: Double = 0.0,
    /** Durée moyenne d'un tour en secondes (parties chronométrées) ; 0 si inconnue. */
    val averageSecondsPerTurn: Double = 0.0,
    val turnBuckets: List<CountBucket> = emptyList(),
    val topAvatars: List<LabelCount> = emptyList(),
)
