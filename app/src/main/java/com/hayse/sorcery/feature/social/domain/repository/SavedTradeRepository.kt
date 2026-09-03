package com.hayse.sorcery.feature.social.domain.repository

import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.SavedTradeItem
import kotlinx.coroutines.flow.Flow

/**
 * Échanges planifiés avec les pairs rencontrés en room, persistés pour être appliqués plus tard.
 * Les lignes d'échange sont exprimées en [MatchLine] `(slug, finish, quantity)`.
 */
interface SavedTradeRepository {

    fun observeSavedTrades(): Flow<List<SavedTradeItem>>

    suspend fun saveTrade(peerPseudo: String, iGive: List<MatchLine>, iReceive: List<MatchLine>)

    suspend fun deleteTrade(id: Long)

    /**
     * Applique l'échange à la collection locale : décrémente chaque carte donnée, incrémente chaque
     * carte reçue, puis marque l'échange comme réalisé.
     */
    suspend fun applyTrade(id: Long)
}
