package com.hayse.sorcery.feature.cards.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardEntity
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import com.hayse.sorcery.feature.deck.data.local.dao.DeckDao
import com.hayse.sorcery.feature.deck.data.local.entity.DeckCardEntity
import com.hayse.sorcery.feature.deck.data.local.entity.DeckEntity
import com.hayse.sorcery.feature.social.data.local.dao.SocialDao
import com.hayse.sorcery.feature.social.data.local.entity.SavedTradeEntity
import com.hayse.sorcery.feature.social.data.local.entity.TradeableEntryEntity
import com.hayse.sorcery.feature.social.data.local.entity.WantedEntryEntity

@Database(
    entities = [
        CardEntity::class,
        PrintingEntity::class,
        CollectionEntryEntity::class,
        DeckEntity::class,
        DeckCardEntity::class,
        TradeableEntryEntity::class,
        WantedEntryEntity::class,
        SavedTradeEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class SorceryDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun collectionDao(): CollectionDao
    abstract fun deckDao(): DeckDao
    abstract fun socialDao(): SocialDao

    companion object {
        /** v6 : zone « collection » (réserve de 10 cartes) des decks, sans perdre les données existantes. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE deck_cards ADD COLUMN collectionQuantity INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
    }
}
