package com.hayse.sorcery.feature.cards.domain.usecase

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow

class ObserveCardsUseCase(private val repository: CardRepository) {
    operator fun invoke(filter: CardFilter): Flow<List<SetEntry<Card>>> = repository.observeCards(filter)
}

class GetCardDetailUseCase(private val repository: CardRepository) {
    suspend operator fun invoke(name: String): CardDetail? = repository.getCard(name)
}
