package com.hayse.sorcery.feature.cards.domain.repository

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    /** Peuple le catalogue au premier appel (idempotent). */
    suspend fun ensureSeeded()

    fun observeCards(filter: CardFilter): Flow<List<Card>>

    suspend fun getCard(name: String): CardDetail?

    suspend fun availableTypes(): List<String>

    suspend fun availableSets(): List<String>
}
