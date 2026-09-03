package com.hayse.sorcery.feature.social.domain.repository

import com.hayse.sorcery.feature.social.domain.model.TradeableCopy
import com.hayse.sorcery.feature.social.domain.model.TradeableItem
import com.hayse.sorcery.feature.social.domain.model.WantedCopy
import com.hayse.sorcery.feature.social.domain.model.WantedItem
import kotlinx.coroutines.flow.Flow

/**
 * Listes locales « à échanger » et « recherché ». Les clés (slug + finish) alimenteront
 * directement les payloads P2P des étapes suivantes.
 */
interface TradeListRepository {

    fun observeTradeable(): Flow<List<TradeableItem>>

    fun observeWanted(): Flow<List<WantedItem>>

    /** Fixe la quantité proposée pour une impression ; bornée à la quantité possédée. */
    suspend fun setTradeable(slug: String, finish: String, quantity: Int)

    /** Fixe la quantité recherchée ; `0` supprime l'entrée. */
    suspend fun setWanted(slug: String, finish: String, quantity: Int)

    /** Instantané des impressions proposées (quantité re-bornée au possédé actuel). */
    suspend fun currentTradeableCopies(): List<TradeableCopy>

    /** Instantané des impressions recherchées. */
    suspend fun currentWantedCopies(): List<WantedCopy>
}
