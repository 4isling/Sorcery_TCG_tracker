package com.hayse.sorcery.feature.social.data.p2p.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Message échangé en continu pendant une session de room interactive (mode 1:1). Contrairement au
 * [TradePayload] one-shot, une session émet plusieurs messages de types différents (poignée de
 * main, chat, partage de collection, offres d'échange). La sérialisation est polymorphe : le champ
 * discriminant `type` (voir `RoomMessageCodec`) porte le [SerialName] de chaque variante.
 *
 * Les cartes sont toujours référencées par `(slug, finish)` via [PayloadEntry] : le pair résout
 * localement les métadonnées et images depuis sa propre base.
 */
@Serializable
sealed interface RoomMessage {

    /** Poignée de main d'ouverture : annonce le pseudo et la version de protocole. */
    @Serializable
    @SerialName("hello")
    data class Hello(
        val pseudo: String,
        val schemaVersion: Int = SCHEMA_VERSION,
    ) : RoomMessage

    /** Message de chat texte, horodaté par l'émetteur (millis epoch). */
    @Serializable
    @SerialName("chat")
    data class Chat(
        val text: String,
        val sentAt: Long,
    ) : RoomMessage

    /** Instantané complet de la collection partagée avec le pair. */
    @Serializable
    @SerialName("collection")
    data class CollectionSnapshot(
        val entries: List<PayloadEntry> = emptyList(),
    ) : RoomMessage

    /** Listes « à échanger » / « recherché » optionnelles, pour le calcul de suggestions. */
    @Serializable
    @SerialName("lists")
    data class TradeLists(
        val tradeable: List<PayloadEntry> = emptyList(),
        val wanted: List<PayloadEntry> = emptyList(),
    ) : RoomMessage

    /** Proposition d'échange concrète, identifiée par [offerId] du point de vue de l'émetteur. */
    @Serializable
    @SerialName("offer")
    data class TradeOffer(
        val offerId: String,
        val iGive: List<PayloadEntry> = emptyList(),
        val iReceive: List<PayloadEntry> = emptyList(),
    ) : RoomMessage

    /** Réponse à une [TradeOffer] : acceptée ou refusée. */
    @Serializable
    @SerialName("response")
    data class TradeResponse(
        val offerId: String,
        val accepted: Boolean,
    ) : RoomMessage
}
