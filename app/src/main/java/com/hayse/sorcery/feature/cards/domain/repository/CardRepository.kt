package com.hayse.sorcery.feature.cards.domain.repository

import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    /** Peuple le catalogue au premier appel (idempotent). */
    suspend fun ensureSeeded()

    /** Une entrée par (carte, set d'impression) pour le rangement par set. */
    fun observeCards(filter: CardFilter): Flow<List<SetEntry<Card>>>

    suspend fun getCard(name: String): CardDetail?

    suspend fun availableTypes(): List<String>

    suspend fun availableSets(): List<String>
}
