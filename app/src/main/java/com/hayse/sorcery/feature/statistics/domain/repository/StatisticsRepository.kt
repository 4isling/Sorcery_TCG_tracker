package com.hayse.sorcery.feature.statistics.domain.repository

import com.hayse.sorcery.feature.statistics.domain.model.CollectionStats
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    /** Statistiques agrégées de la collection possédée, réactives aux changements. */
    fun observeStats(): Flow<CollectionStats>
}
