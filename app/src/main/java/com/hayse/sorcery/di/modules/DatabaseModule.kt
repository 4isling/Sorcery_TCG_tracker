package com.hayse.sorcery.di.modules

import androidx.room.Room
import com.hayse.sorcery.feature.cards.data.local.SorceryDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Base Room partagée (cartes M2, collection M3). Peuplée au premier lancement via CardCatalogSeeder.
// Les migrations connues préservent les données ; à défaut, la base est recréée (fallback destructif).
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            SorceryDatabase::class.java,
            "sorcery.db",
        )
            .addMigrations(SorceryDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<SorceryDatabase>().cardDao() }
    single { get<SorceryDatabase>().collectionDao() }
    single { get<SorceryDatabase>().deckDao() }
    single { get<SorceryDatabase>().socialDao() }
}
