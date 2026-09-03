package com.hayse.sorcery.feature.social.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Échange planifié avec une personne rencontrée en room, conservé pour être réalisé/appliqué plus
 * tard. Les cartes à donner/recevoir sont stockées en JSON (`List<PayloadEntry>`) pour rester
 * indépendantes du catalogue courant. [done] passe à vrai une fois l'échange appliqué à la
 * collection.
 */
@Entity(tableName = "saved_trades")
data class SavedTradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val peerPseudo: String,
    val createdAt: Long,
    val giveJson: String,
    val receiveJson: String,
    val done: Boolean = false,
)
