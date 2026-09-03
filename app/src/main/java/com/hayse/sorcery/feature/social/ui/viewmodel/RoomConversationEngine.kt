package com.hayse.sorcery.feature.social.ui.viewmodel

import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.social.data.p2p.MatchComputations
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.data.p2p.model.TradePayload
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.domain.repository.CardCatalog
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import com.hayse.sorcery.feature.social.ui.viewmodel.state.ChatLine
import com.hayse.sorcery.feature.social.ui.viewmodel.state.OfferView
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomTab
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Cœur d'une conversation 1:1 (chat, collections, suggestions, offres) **agnostique au transport**.
 * Partagé entre la Room 1:1 par appairage (`RoomViewModel`) et chaque conversation privée du salon
 * « Global » (`GlobalRoomViewModel`) : le seul point d'injection est [send] (ici `RoomSession.send`,
 * là `MeshSession.sendTo(peerId, …)`).
 *
 * Opère sur un [state] `MutableStateFlow<RoomViewState>` **partagé** avec l'appelant : le moteur ne
 * touche que les champs de conversation ; l'appelant garde la main sur les champs d'appairage.
 * Le handshake `Hello` est volontairement hors de ce moteur (géré par l'appelant : contrôle de
 * version côté Room, roster côté maillage).
 */
class RoomConversationEngine(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<RoomViewState>,
    private val tradeLists: TradeListRepository,
    private val savedTrades: SavedTradeRepository,
    private val collection: CollectionRepository,
    private val catalog: CardCatalog,
    private val send: suspend (RoomMessage) -> Unit,
) {

    /** Ma dernière charge utile (listes), pour recalculer les suggestions à l'arrivée des listes du pair. */
    private var myPayload: TradePayload = TradePayload()

    /** Offres que j'ai proposées, indexées par id, de mon point de vue (à appliquer si acceptées). */
    private val outgoingOffers = HashMap<String, Pair<List<MatchLine>, List<MatchLine>>>()

    /** Trames initiales à émettre par l'appelant (l'orchestration d'envoi diffère selon le transport). */
    data class InitialPayload(
        val entries: List<PayloadEntry>,
        val tradeable: List<PayloadEntry>,
        val wanted: List<PayloadEntry>,
    )

    /** Prépare ma collection + mes listes, met à jour `myCollection` résolue, renvoie de quoi émettre. */
    suspend fun prepareLocal(pseudo: String): InitialPayload {
        val entries = currentCollectionEntries()
        val tradeable = tradeLists.currentTradeableCopies().map { PayloadEntry(it.slug, it.finish, it.quantity) }
        val wanted = tradeLists.currentWantedCopies().map { PayloadEntry(it.slug, it.finish, it.quantity) }
        myPayload = TradePayload(pseudo = pseudo, tradeable = tradeable, wanted = wanted)
        val resolved = catalog.resolve(entries.toMatchLines())
        state.update { it.copy(myCollection = resolved) }
        return InitialPayload(entries, tradeable, wanted)
    }

    /** Traite un message reçu (hors `Hello`, géré par l'appelant). */
    suspend fun handleIncoming(message: RoomMessage) {
        when (message) {
            is RoomMessage.Hello -> Unit

            is RoomMessage.Chat ->
                state.update { it.copy(messages = it.messages + ChatLine(false, message.text, message.sentAt)) }

            is RoomMessage.CollectionSnapshot -> {
                val resolved = catalog.resolve(message.entries.toMatchLines())
                state.update { it.copy(peerCollection = resolved) }
            }

            is RoomMessage.TradeLists -> {
                val peerPayload = TradePayload(
                    pseudo = state.value.peerPseudo,
                    tradeable = message.tradeable,
                    wanted = message.wanted,
                )
                val match = MatchComputations.compute(myPayload, peerPayload)
                state.update { it.copy(suggestions = match) }
            }

            is RoomMessage.TradeOffer -> {
                // Point de vue du pair → mien : ce qu'il reçoit, je le donne ; ce qu'il donne, je le reçois.
                val iGive = catalog.resolve(message.iReceive.toMatchLines())
                val iReceive = catalog.resolve(message.iGive.toMatchLines())
                state.update { it.copy(incomingOffer = OfferView(message.offerId, iGive, iReceive)) }
            }

            is RoomMessage.TradeResponse -> {
                if (message.accepted) {
                    outgoingOffers[message.offerId]?.let { (give, receive) -> applyToCollection(give, receive) }
                }
                outgoingOffers.remove(message.offerId)
            }
        }
    }

    fun setTab(tab: RoomTab) {
        state.update { it.copy(roomTab = tab) }
    }

    fun sendChat(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val sentAt = System.currentTimeMillis()
        state.update { it.copy(messages = it.messages + ChatLine(true, trimmed, sentAt)) }
        scope.launch { send(RoomMessage.Chat(trimmed, sentAt)) }
    }

    /** Ré-envoie ma collection au pair (utile après une mise à jour). */
    fun shareCollection() {
        scope.launch {
            val entries = currentCollectionEntries()
            state.update { it.copy(myCollection = catalog.resolve(entries.toMatchLines())) }
            send(RoomMessage.CollectionSnapshot(entries))
        }
    }

    /** Ré-envoie mes listes à échanger / recherchées (aide aux suggestions). */
    fun shareLists() {
        scope.launch {
            val tradeable = tradeLists.currentTradeableCopies().map { PayloadEntry(it.slug, it.finish, it.quantity) }
            val wanted = tradeLists.currentWantedCopies().map { PayloadEntry(it.slug, it.finish, it.quantity) }
            myPayload = myPayload.copy(tradeable = tradeable, wanted = wanted)
            send(RoomMessage.TradeLists(tradeable, wanted))
        }
    }

    /** Propose un échange (point de vue local : [iGive] cartes données, [iReceive] cartes voulues). */
    fun proposeTrade(iGive: List<MatchLine>, iReceive: List<MatchLine>) {
        if (iGive.isEmpty() && iReceive.isEmpty()) return
        val offerId = UUID.randomUUID().toString()
        outgoingOffers[offerId] = iGive to iReceive
        scope.launch { send(RoomMessage.TradeOffer(offerId, iGive.toPayload(), iReceive.toPayload())) }
    }

    /** Répond à l'offre reçue. Si acceptée, j'applique ma part à ma collection. */
    fun respondOffer(accept: Boolean) {
        val offer = state.value.incomingOffer ?: return
        scope.launch {
            send(RoomMessage.TradeResponse(offer.offerId, accept))
            if (accept) applyToCollection(offer.iGive.toMatchLines(), offer.iReceive.toMatchLines())
        }
        state.update { it.copy(incomingOffer = null) }
    }

    /** Sauvegarde l'offre reçue pour l'appliquer plus tard ; la retire des offres en attente. */
    fun saveIncomingForLater() {
        val offer = state.value.incomingOffer ?: return
        val peer = state.value.peerPseudo ?: "?"
        scope.launch {
            savedTrades.saveTrade(peer, offer.iGive.toMatchLines(), offer.iReceive.toMatchLines())
            send(RoomMessage.TradeResponse(offer.offerId, accepted = false))
        }
        state.update { it.copy(incomingOffer = null) }
    }

    /** Sauvegarde une offre composée localement pour plus tard (sans l'envoyer). */
    fun saveComposedForLater(iGive: List<MatchLine>, iReceive: List<MatchLine>) {
        if (iGive.isEmpty() && iReceive.isEmpty()) return
        val peer = state.value.peerPseudo ?: "?"
        scope.launch { savedTrades.saveTrade(peer, iGive, iReceive) }
    }

    /** Réinitialise l'état interne (offres en attente, listes). */
    fun reset() {
        outgoingOffers.clear()
        myPayload = TradePayload()
    }

    private suspend fun applyToCollection(iGive: List<MatchLine>, iReceive: List<MatchLine>) {
        iGive.forEach { collection.adjustQuantity(it.slug, it.finish, -it.quantity) }
        iReceive.forEach { collection.adjustQuantity(it.slug, it.finish, it.quantity) }
        shareCollection()
    }

    private suspend fun currentCollectionEntries(): List<PayloadEntry> =
        collection.observeCollection(CollectionFilter()).first()
            .flatMap { item -> item.copies }
            .filter { it.quantity > 0 }
            .map { PayloadEntry(it.slug, it.finish, it.quantity) }
}

internal fun List<PayloadEntry>.toMatchLines(): List<MatchLine> =
    map { MatchLine(it.slug, it.finish, it.qty) }

internal fun List<MatchLine>.toPayload(): List<PayloadEntry> =
    map { PayloadEntry(it.slug, it.finish, it.quantity) }

@JvmName("tradeCardLinesToMatchLines")
internal fun List<TradeCardLine>.toMatchLines(): List<MatchLine> =
    map { MatchLine(it.printing.slug, it.printing.finish, it.quantity) }
