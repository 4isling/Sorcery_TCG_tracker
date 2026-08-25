package com.hayse.sorcery.feature.cards.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "printings",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["name"],
            childColumns = ["cardName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("cardName")],
)
data class PrintingEntity(
    @PrimaryKey val slug: String,
    val cardName: String,
    val setName: String,
    val releasedAt: String,
    val finish: String,
    val product: String,
    val artist: String,
    val flavorText: String,
    val typeText: String,
    val source: String,
)
