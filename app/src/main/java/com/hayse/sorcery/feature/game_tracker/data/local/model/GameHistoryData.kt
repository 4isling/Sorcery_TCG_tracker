package com.hayse.sorcery.feature.game_tracker.data.local.model

import com.hayse.sorcery.feature.game_tracker.domain.model.GameRecord
import com.hayse.sorcery.feature.game_tracker.domain.model.PlayerId
import kotlinx.serialization.Serializable

/** Liste persistée des parties terminées (DataStore `game_history.json`). */
@Serializable
data class GameHistoryData(
    val records: List<GameRecordData> = emptyList(),
)

@Serializable
data class GameRecordData(
    val playedAt: Long,
    val playerOnePseudo: String? = null,
    val playerOneAvatar: String? = null,
    val playerTwoPseudo: String? = null,
    val playerTwoAvatar: String? = null,
    /** Nom d'enum de PlayerId, ou null pour un nul/abandon. */
    val winner: String? = null,
    val turns: Int,
    val durationSeconds: Int? = null,
)

fun GameRecord.toData(): GameRecordData = GameRecordData(
    playedAt = playedAt,
    playerOnePseudo = playerOnePseudo,
    playerOneAvatar = playerOneAvatar,
    playerTwoPseudo = playerTwoPseudo,
    playerTwoAvatar = playerTwoAvatar,
    winner = winner?.name,
    turns = turns,
    durationSeconds = durationSeconds,
)

fun GameRecordData.toDomain(): GameRecord = GameRecord(
    playedAt = playedAt,
    playerOnePseudo = playerOnePseudo,
    playerOneAvatar = playerOneAvatar,
    playerTwoPseudo = playerTwoPseudo,
    playerTwoAvatar = playerTwoAvatar,
    winner = winner?.let { PlayerId.valueOf(it) },
    turns = turns,
    durationSeconds = durationSeconds,
)
