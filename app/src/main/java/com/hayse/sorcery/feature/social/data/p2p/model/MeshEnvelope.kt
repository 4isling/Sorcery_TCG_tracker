package com.hayse.sorcery.feature.social.data.p2p.model

import kotlinx.serialization.Serializable

/**
 * Enveloppe des trames échangées sur le maillage « Global » (cluster à N pairs). Contrairement au
 * 1:1 par appairage (où le pair unique est implicite), chaque trame doit dire QUI l'émet, et une
 * trame privée QUI la reçoit.
 *
 * Le [body] réutilise tel quel le protocole 1:1 [RoomMessage] : la sérialisation reste polymorphe
 * (le body garde son discriminant `type`), l'enveloppe n'ajoute que des champs sœurs. Ainsi le
 * chemin QR 1:1 (RoomMessage nu) et le chemin maillé (enveloppé) partagent la même logique métier.
 *
 * [senderId] est le `deviceId` stable (voir Réglages), source de vérité de l'identité — découplé de
 * l'`endpointId` Nearby, éphémère. [recipientId] null = diffusion à tout le cluster ; non-null =
 * conversation privée logique adressée à un pair précis.
 */
@Serializable
data class MeshEnvelope(
    val senderId: String,
    val senderPseudo: String,
    val recipientId: String? = null,
    val body: RoomMessage,
    val schemaVersion: Int = SCHEMA_VERSION,
)
