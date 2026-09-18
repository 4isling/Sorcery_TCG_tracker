package com.hayse.sorcery.feature.deckformat.domain.usecase

import com.hayse.sorcery.core.shared.model.Ownership
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.deckformat.domain.DeckValidator
import com.hayse.sorcery.feature.deckformat.domain.PlayableCards
import com.hayse.sorcery.feature.deckformat.domain.model.Deck
import com.hayse.sorcery.feature.deckformat.domain.model.DeckFormat
import com.hayse.sorcery.feature.deckformat.domain.model.DeckValidation
import com.hayse.sorcery.feature.deckformat.domain.model.PlayableCard
import kotlinx.coroutines.flow.first

class ValidateDeckUseCase(private val cards: CardRepository) {
    suspend operator fun invoke(deck: Deck, format: DeckFormat): DeckValidation {
        val cardsByName: Map<String, Card> = cards.observeCards(CardFilter()).first()
            .associate { it.card.name to it.card }
        return DeckValidator.validate(deck, format, cardsByName)
    }
}

class GetPlayableCardsUseCase(private val collection: CollectionRepository) {
    suspend operator fun invoke(format: DeckFormat): List<PlayableCard> {
        val owned = collection.observeCollection(CollectionFilter(ownership = Ownership.Owned)).first()
            .map { it.payload.card to it.payload.totalQuantity }
        return PlayableCards.compute(owned, format)
    }
}