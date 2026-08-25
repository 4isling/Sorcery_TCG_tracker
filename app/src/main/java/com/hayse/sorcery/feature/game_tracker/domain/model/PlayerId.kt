package com.hayse.sorcery.feature.game_tracker.domain.model

enum class PlayerId {
    One,
    Two;

    fun other(): PlayerId = if (this == One) Two else One
}
