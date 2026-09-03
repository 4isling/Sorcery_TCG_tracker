package com.hayse.sorcery.feature.social.domain.p2p

/** Rôle d'un appareil dans une session d'appairage 1:1 (mode A). */
enum class PairingRole {
    /** Diffuse une session à rejoindre (advertise). */
    HOST,

    /** Rejoint une session diffusée (discover). */
    GUEST,
}
