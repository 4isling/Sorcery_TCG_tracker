package com.hayse.sorcery.feature.cards.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardEntity
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity
import com.hayse.sorcery.feature.collection.data.local.dao.CollectionDao
import com.hayse.sorcery.feature.deck.data.local.dao.DeckDao
import com.hayse.sorcery.feature.deck.data.local.entity.DeckCardEntity
import com.hayse.sorcery.feature.deck.data.local.entity.DeckEntity

@Database(
    entities = [
        CardEntity::class,
        PrintingEntity::class,
        CollectionEntryEntity::class,
        DeckEntity::class,
        DeckCardEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class SorceryDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun collectionDao(): CollectionDao
    abstract fun deckDao(): DeckDao
}
