package com.hayse.sorcery.feature.cards.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hayse.sorcery.feature.cards.data.local.entity.CardEntity
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity
import com.hayse.sorcery.feature.cards.data.local.model.PrintingKey
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrintings(printings: List<PrintingEntity>)

    @Transaction
    @Query(
        """
        SELECT * FROM cards
        WHERE (:query IS NULL OR name LIKE '%' || :query || '%')
          AND (:element IS NULL OR elements LIKE '%' || :element || '%')
          AND (:type IS NULL OR type = :type)
          AND (:rarity IS NULL OR rarity = :rarity)
          AND (:setName IS NULL OR EXISTS (
                SELECT 1 FROM printings p
                WHERE p.cardName = cards.name AND p.setName = :setName))
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeCards(
        query: String?,
        element: String?,
        type: String?,
        rarity: String?,
        setName: String?,
    ): Flow<List<CardWithPrintings>>

    @Transaction
    @Query("SELECT * FROM cards WHERE name = :name LIMIT 1")
    suspend fun getCardWithPrintings(name: String): CardWithPrintings?

    @Query("SELECT DISTINCT type FROM cards ORDER BY type COLLATE NOCASE ASC")
    suspend fun distinctTypes(): List<String>

    @Query("SELECT DISTINCT setName FROM printings ORDER BY setName COLLATE NOCASE ASC")
    suspend fun distinctSets(): List<String>

    @Query("SELECT slug, cardName, setName, finish, product FROM printings")
    suspend fun allPrintingKeys(): List<PrintingKey>
}
