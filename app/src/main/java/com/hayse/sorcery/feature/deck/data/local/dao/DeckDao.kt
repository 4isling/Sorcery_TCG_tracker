package com.hayse.sorcery.feature.deck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.hayse.sorcery.feature.deck.data.local.entity.DeckCardEntity
import com.hayse.sorcery.feature.deck.data.local.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Insert
    suspend fun insertDeck(deck: DeckEntity): Long

    @Query("UPDATE decks SET name = :name, updatedAt = :updatedAt WHERE id = :deckId")
    suspend fun renameDeck(deckId: Long, name: String, updatedAt: Long)

    @Query("UPDATE decks SET updatedAt = :updatedAt WHERE id = :deckId")
    suspend fun touch(deckId: Long, updatedAt: Long)

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: Long)

    @Query("SELECT * FROM decks ORDER BY updatedAt DESC")
    fun observeDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    fun observeDeck(deckId: Long): Flow<List<DeckEntity>>

    @Query("SELECT * FROM deck_cards")
    fun observeAllDeckCards(): Flow<List<DeckCardEntity>>

    @Query("SELECT * FROM deck_cards WHERE deckId = :deckId")
    fun observeDeckCards(deckId: Long): Flow<List<DeckCardEntity>>

    @Query("SELECT quantity FROM deck_cards WHERE deckId = :deckId AND cardName = :cardName")
    suspend fun getQuantity(deckId: Long, cardName: String): Int?

    @Upsert
    suspend fun upsert(entry: DeckCardEntity)

    @Query("DELETE FROM deck_cards WHERE deckId = :deckId AND cardName = :cardName")
    suspend fun delete(deckId: Long, cardName: String)
}
