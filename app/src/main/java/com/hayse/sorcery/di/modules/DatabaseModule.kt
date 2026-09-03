package com.hayse.sorcery.di.modules

import androidx.room.Room
import com.hayse.sorcery.feature.cards.data.local.SorceryDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Base Room partagée (cartes M2, collection M3). Peuplée au premier lancement via CardCatalogSeeder.
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            SorceryDatabase::class.java,
            "sorcery.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<SorceryDatabase>().cardDao() }
    single { get<SorceryDatabase>().collectionDao() }
    single { get<SorceryDatabase>().deckDao() }
    single { get<SorceryDatabase>().socialDao() }
}
