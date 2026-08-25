package com.hayse.sorcery.feature.cards.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val name: String,
    val type: String,
    val rarity: String,
    val rulesText: String,
    val cost: Int?,
    val attack: Int?,
    val defence: Int?,
    val life: Int?,
    val thAir: Int,
    val thEarth: Int,
    val thFire: Int,
    val thWater: Int,
    val elements: String,
    val subTypes: String,
)
