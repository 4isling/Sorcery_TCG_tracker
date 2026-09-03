package com.hayse.sorcery.feature.social.data.p2p.model

import com.hayse.sorcery.feature.social.domain.model.TradeableCopy
import com.hayse.sorcery.feature.social.domain.model.WantedCopy
import kotlinx.serialization.Serializable

/** Version du schéma des payloads P2P. À incrémenter à tout changement incompatible. */
const val SCHEMA_VERSION: Int = 3

/** Une ligne de payload : impression (`slug` + `finish`) et quantité concernée. */
@Serializable
data class PayloadEntry(
    val slug: String,
    val finish: String,
    val qty: Int,
)

/**
 * Charge utile échangée entre deux téléphones lors d'une session d'appairage. Versionnée
 * ([schemaVersion]) pour permettre une évolution compatible ascendante. Les clés `(slug, finish)`
 * sont identiques à celles des tables locales : l'aller-retour est sans perte.
 */
@Serializable
data class TradePayload(
    val schemaVersion: Int = SCHEMA_VERSION,
    val pseudo: String? = null,
    val tradeable: List<PayloadEntry> = emptyList(),
    val wanted: List<PayloadEntry> = emptyList(),
)

fun tradePayloadOf(
    pseudo: String?,
    tradeable: List<TradeableCopy>,
    wanted: List<WantedCopy>,
): TradePayload = TradePayload(
    schemaVersion = SCHEMA_VERSION,
    pseudo = pseudo,
    tradeable = tradeable.map { PayloadEntry(it.slug, it.finish, it.quantity) },
    wanted = wanted.map { PayloadEntry(it.slug, it.finish, it.quantity) },
)
