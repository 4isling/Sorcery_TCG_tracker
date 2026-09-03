package com.hayse.sorcery.feature.social.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.hayse.sorcery.feature.social.data.local.entity.SavedTradeEntity
import com.hayse.sorcery.feature.social.data.local.entity.TradeableEntryEntity
import com.hayse.sorcery.feature.social.data.local.entity.WantedEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    @Query("SELECT * FROM trade_tradeable WHERE quantity > 0")
    fun observeTradeable(): Flow<List<TradeableEntryEntity>>

    @Query("SELECT * FROM trade_wanted WHERE quantity > 0")
    fun observeWanted(): Flow<List<WantedEntryEntity>>

    @Query("SELECT * FROM trade_tradeable WHERE quantity > 0")
    suspend fun getTradeable(): List<TradeableEntryEntity>

    @Query("SELECT * FROM trade_wanted WHERE quantity > 0")
    suspend fun getWanted(): List<WantedEntryEntity>

    @Upsert
    suspend fun upsertTradeable(entry: TradeableEntryEntity)

    @Upsert
    suspend fun upsertWanted(entry: WantedEntryEntity)

    @Query("DELETE FROM trade_tradeable WHERE printingSlug = :slug AND finish = :finish")
    suspend fun deleteTradeable(slug: String, finish: String)

    @Query("DELETE FROM trade_wanted WHERE printingSlug = :slug AND finish = :finish")
    suspend fun deleteWanted(slug: String, finish: String)

    @Query("DELETE FROM trade_tradeable")
    suspend fun clearTradeable()

    @Query("DELETE FROM trade_wanted")
    suspend fun clearWanted()

    @Query("SELECT * FROM saved_trades ORDER BY createdAt DESC")
    fun observeSavedTrades(): Flow<List<SavedTradeEntity>>

    @Query("SELECT * FROM saved_trades WHERE id = :id")
    suspend fun getSavedTrade(id: Long): SavedTradeEntity?

    @Insert
    suspend fun insertSavedTrade(entry: SavedTradeEntity): Long

    @Query("DELETE FROM saved_trades WHERE id = :id")
    suspend fun deleteSavedTrade(id: Long)

    @Query("UPDATE saved_trades SET done = :done WHERE id = :id")
    suspend fun setSavedTradeDone(id: Long, done: Boolean)
}
