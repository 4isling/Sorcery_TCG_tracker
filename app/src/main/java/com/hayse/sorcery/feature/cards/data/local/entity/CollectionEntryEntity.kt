package com.hayse.sorcery.feature.cards.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Quantité possédée d'une impression, par finish (Standard/Foil). PK composite (impression, finish). */
@Entity(
    tableName = "collection_entries",
    primaryKeys = ["printingSlug", "finish"],
    foreignKeys = [
        ForeignKey(
            entity = PrintingEntity::class,
            parentColumns = ["slug"],
            childColumns = ["printingSlug"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("printingSlug")],
)
data class CollectionEntryEntity(
    val printingSlug: String,
    val finish: String,
    val quantity: Int,
)
