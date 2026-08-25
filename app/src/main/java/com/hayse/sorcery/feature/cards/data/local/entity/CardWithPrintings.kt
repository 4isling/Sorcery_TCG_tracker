package com.hayse.sorcery.feature.cards.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CardWithPrintings(
    @Embedded val card: CardEntity,
    @Relation(parentColumn = "name", entityColumn = "cardName")
    val printings: List<PrintingEntity>,
)
