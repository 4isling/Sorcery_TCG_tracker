package com.hayse.sorcery.feature.social.ui.viewmodel.state

import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.domain.model.TradeMatch
import com.hayse.sorcery.feature.social.domain.p2p.PairingRole

/** Étape de vie de la room 1:1. */
enum class RoomPhase { Pairing, Connecting, InRoom, Failed }

/** Onglets internes affichés une fois la room ouverte. */
enum class RoomTab { Chat, PeerCollection, Trade }

/** Cause d'échec d'une room, pour un message clair à l'utilisateur. */
enum class RoomFailure { TRANSPORT, INCOMPATIBLE_VERSION }

/** Une ligne de chat, du point de vue local ([fromMe]). */
data class ChatLine(
    val fromMe: Boolean,
    val text: String,
    val sentAt: Long,
)

/** Une offre d'échange affichable (du point de vue local), résolue en cartes. */
data class OfferView(
    val offerId: String,
    val iGive: List<TradeCardLine>,
    val iReceive: List<TradeCardLine>,
)

/**
 * État complet de l'écran Room. Regroupe la phase d'appairage (réutilisée de l'ancien
 * `PairingViewState`) et la session vivante (chat, collection du pair, offres).
 *
 * @param phase étape courante (appairage → connexion → room, ou échec).
 * @param role rôle choisi (`null` tant que non choisi).
 * @param hostCode code de session généré quand on héberge.
 * @param hostQrPayload token encodé à afficher en QR quand on héberge.
 * @param guestCodeInput code saisi quand on rejoint.
 * @param permissionsGranted vrai quand les permissions Nearby sont accordées.
 * @param scanning vrai quand l'aperçu caméra de scan QR est ouvert (invité).
 * @param myPseudo pseudo local (réglages, ou nom d'appareil par défaut).
 * @param peerPseudo pseudo du pair (reçu au handshake `Hello`).
 * @param roomTab onglet actif dans la room.
 * @param messages fil de discussion.
 * @param myCollection ma collection résolue (pour composer une offre).
 * @param peerCollection collection du pair résolue (pour l'afficher / composer).
 * @param suggestions correspondances calculées à partir des listes (aide optionnelle).
 * @param incomingOffer offre reçue en attente de réponse (`null` = aucune).
 * @param failure cause d'échec quand [phase] vaut [RoomPhase.Failed].
 */
data class RoomViewState(
    val phase: RoomPhase = RoomPhase.Pairing,
    val role: PairingRole? = null,
    val hostCode: String? = null,
    val hostQrPayload: String? = null,
    val guestCodeInput: String = "",
    val permissionsGranted: Boolean = false,
    val scanning: Boolean = false,
    val myPseudo: String = "",
    val peerPseudo: String? = null,
    val roomTab: RoomTab = RoomTab.Chat,
    val messages: List<ChatLine> = emptyList(),
    val myCollection: List<TradeCardLine> = emptyList(),
    val peerCollection: List<TradeCardLine> = emptyList(),
    val suggestions: TradeMatch? = null,
    val incomingOffer: OfferView? = null,
    val failure: RoomFailure? = null,
) {
    val canStart: Boolean
        get() = permissionsGranted && phase == RoomPhase.Pairing && when (role) {
            PairingRole.HOST -> !hostCode.isNullOrEmpty()
            PairingRole.GUEST -> guestCodeInput.isNotBlank()
            null -> false
        }
}
