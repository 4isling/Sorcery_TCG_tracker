package com.hayse.sorcery.feature.statistics.data.repository

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import com.hayse.sorcery.feature.statistics.domain.model.CollectionStats
import com.hayse.sorcery.feature.statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class StatisticsRepositoryImpl(
    private val cardDao: CardDao,
    private val collectionDao: CollectionDao,
) : StatisticsRepository {

    override fun observeStats(): Flow<CollectionStats> =
        combine(
            cardDao.observeCards(null, null, null, null, null),
            collectionDao.observeEntries(),
        ) { cards, entries -> StatisticsComputations.compute(cards, entries) }
}
