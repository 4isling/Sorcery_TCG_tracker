package com.hayse.sorcery.feature.social.domain.p2p

/**
 * Jeton d'appairage échangé par le canal de bootstrap (NFC/QR). Volontairement minuscule :
 * il ne transporte **jamais** les listes d'échange, seulement de quoi ouvrir une session
 * ([sessionId]) et vérifier la compatibilité du protocole ([schemaVersion]).
 */
data class SessionToken(
    val sessionId: String,
    val schemaVersion: Int,
)
