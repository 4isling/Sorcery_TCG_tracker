package com.hayse.sorcery.feature.social.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity

/** Quantité recherchée d'une impression, par finish (carte pas forcément possédée). */
@Entity(
    tableName = "trade_wanted",
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
data class WantedEntryEntity(
    val printingSlug: String,
    val finish: String,
    val quantity: Int,
)
