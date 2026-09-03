package com.hayse.sorcery.feature.social.domain.p2p

/**
 * Amorce l'appairage 1:1 en produisant/lisant un [SessionToken] court, indépendamment du
 * canal physique (NFC en priorité, QR en repli — étape 4). Le token ne contient aucune donnée
 * d'échange : il sert uniquement à ouvrir ensuite un [LocalTransport].
 */
interface PairingBootstrap {

    /** Hôte : génère un token de session (id aléatoire + version de schéma courante). */
    fun newToken(): SessionToken

    /** Sérialise le token pour le canal de bootstrap (charge utile NFC/QR, courte). */
    fun encodeToken(token: SessionToken): String

    /** Invité : lit un token reçu ; `null` si illisible ou malformé. */
    fun decodeToken(raw: String): SessionToken?
}
