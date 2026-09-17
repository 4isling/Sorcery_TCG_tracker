package com.hayse.sorcery.feature.collection.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collection_entries WHERE quantity > 0")
    fun observeEntries(): Flow<List<CollectionEntryEntity>>

    @Query("SELECT printingSlug FROM collection_entries WHERE quantity > 0")
    fun observeOwnedSlugs(): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT p.cardName FROM collection_entries e
        JOIN printings p ON p.slug = e.printingSlug
        WHERE e.quantity > 0
        """
    )
    fun observeOwnedCardNames(): Flow<List<String>>

    @Query(
        """
        SELECT e.printingSlug AS printingSlug, e.finish AS finish, e.quantity AS quantity
        FROM collection_entries e
        JOIN printings p ON p.slug = e.printingSlug
        WHERE p.cardName = :cardName AND e.quantity > 0
        """
    )
    fun observeForCard(cardName: String): Flow<List<CollectionEntryEntity>>

    @Query("SELECT quantity FROM collection_entries WHERE printingSlug = :slug AND finish = :finish")
    suspend fun getQuantity(slug: String, finish: String): Int?

    @Upsert
    suspend fun upsert(entry: CollectionEntryEntity)

    @Query("DELETE FROM collection_entries WHERE printingSlug = :slug AND finish = :finish")
    suspend fun delete(slug: String, finish: String)

    @Query("DELETE FROM collection_entries")
    suspend fun clearAll()
}
