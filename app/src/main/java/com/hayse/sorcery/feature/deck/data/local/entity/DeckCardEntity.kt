package com.hayse.sorcery.feature.deck.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Une carte d'un deck, identifiée par son nom (PK de `cards`) et non par une impression :
 * un deck raisonne en identité de carte, la possession étant agrégée sur toutes ses impressions.
 */
@Entity(
    tableName = "deck_cards",
    primaryKeys = ["deckId", "cardName"],
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("deckId")],
)
data class DeckCardEntity(
    val deckId: Long,
    val cardName: String,
    val quantity: Int,
)
