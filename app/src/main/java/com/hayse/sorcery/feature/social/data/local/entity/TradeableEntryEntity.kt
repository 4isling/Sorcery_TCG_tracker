package com.hayse.sorcery.feature.social.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity

/** Quantité d'une impression proposée à l'échange, par finish. PK composite (impression, finish). */
@Entity(
    tableName = "trade_tradeable",
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
data class TradeableEntryEntity(
    val printingSlug: String,
    val finish: String,
    val quantity: Int,
)
