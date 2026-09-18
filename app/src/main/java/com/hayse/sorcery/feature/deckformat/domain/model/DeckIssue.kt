package com.hayse.sorcery.feature.deckformat.domain.model

import com.hayse.sorcery.core.shared.model.Rarity

sealed interface DeckIssue {
    data object MissingAvatar : DeckIssue
    data class BannedAvatar(val name: String) : DeckIssue
    data class UnknownCard(val name: String) : DeckIssue
    data class NotADeckCard(val name: String) : DeckIssue
    data class ZoneTooSmall(val zone: DeckZone, val count: Int, val min: Int) : DeckIssue
    data class ZoneTooLarge(val zone: DeckZone, val count: Int, val max: Int) : DeckIssue
    data class WrongZone(val name: String, val zone: DeckZone) : DeckIssue
    data class RarityNotAllowed(val name: String, val rarity: Rarity) : DeckIssue
    data class BannedCard(val name: String) : DeckIssue
    data class TooManyCopies(val name: String, val count: Int, val limit: Int) : DeckIssue
}

data class DeckValidation(val issues: List<DeckIssue>) {
    val isLegal: Boolean get() = issues.isEmpty()
}