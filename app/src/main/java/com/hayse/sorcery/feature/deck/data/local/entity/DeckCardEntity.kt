package com.hayse.sorcery.feature.deck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Une carte d'un deck, identifiée par son nom (PK de `cards`) et non par une impression :
 * un deck raisonne en identité de carte, la possession étant agrégée sur toutes ses impressions.
 *
 * Une même carte peut figurer à la fois dans la zone principale (grimoire/atlas, déduite du type)
 * et dans la collection (réserve de 10 cartes) : [quantity] et [collectionQuantity] sont
 * indépendants. La ligne est supprimée quand les deux tombent à zéro.
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
    @ColumnInfo(defaultValue = "0") val collectionQuantity: Int = 0,
) {
    val isEmpty: Boolean get() = quantity <= 0 && collectionQuantity <= 0
}