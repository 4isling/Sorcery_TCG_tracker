package com.hayse.sorcery.feature.social.ui.viewmodel.state

import com.hayse.sorcery.feature.social.domain.p2p.MeshParticipant

/** Une ligne du chat de groupe, taggée du pseudo de l'expéditeur. */
data class GlobalChatLine(
    val senderPseudo: String,
    val fromMe: Boolean,
    val text: String,
    val sentAt: Long,
)

/**
 * État de l'écran « Global » (salon maillé décentralisé).
 *
 * @param permissionsGranted vrai quand les permissions Nearby sont accordées.
 * @param active vrai quand le maillage advertise/discover (écran au premier plan).
 * @param participants roster courant des pairs à portée.
 * @param messages fil de discussion de groupe.
 * @param openPeerId pair dont la conversation privée est ouverte (`null` = vue roster).
 * @param openPeerPseudo pseudo du pair de la conversation privée ouverte.
 */
data class GlobalRoomViewState(
    val permissionsGranted: Boolean = false,
    val active: Boolean = false,
    val participants: List<MeshParticipant> = emptyList(),
    val messages: List<GlobalChatLine> = emptyList(),
    val openPeerId: String? = null,
    val openPeerPseudo: String? = null,
)
