package com.hayse.sorcery.feature.social.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import com.hayse.sorcery.feature.social.data.p2p.NearbyMeshSession
import com.hayse.sorcery.feature.social.data.p2p.model.MeshEnvelope
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.p2p.MeshConnector
import com.hayse.sorcery.feature.social.domain.p2p.MeshSession
import com.hayse.sorcery.feature.social.domain.p2p.MeshSessionEvent
import com.hayse.sorcery.feature.social.domain.repository.CardCatalog
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import com.hayse.sorcery.feature.social.ui.viewmodel.state.GlobalChatLine
import com.hayse.sorcery.feature.social.ui.viewmodel.state.GlobalRoomViewState
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomPhase
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomTab
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pilote l'écran « Global » : maillage décentralisé (roster + chat de groupe) au-dessus d'un
 * [MeshConnector], et conversations privées 1:1 par pair via un [RoomConversationEngine] dédié.
 *
 * Cycle de vie borné au premier plan : [start] à ON_START, [stop] à ON_STOP (voir l'écran). Le chat
 * de groupe circule en `broadcast` ; chaque conversation privée circule en `sendTo(peerId, …)` sur la
 * même connexion cluster (aucune nouvelle session Nearby).
 */
class GlobalRoomViewModel(
    private val settings: SettingsRepository,
    private val meshConnector: MeshConnector,
    private val tradeLists: TradeListRepository,
    private val savedTrades: SavedTradeRepository,
    private val collection: CollectionRepository,
    private val catalog: CardCatalog,
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalRoomViewState())
    val state: StateFlow<GlobalRoomViewState> = _state.asStateFlow()

    private var session: MeshSession? = null
    private var sessionJob: Job? = null
    private var myDeviceId: String = ""
    private var myPseudo: String = "?"

    /** Une conversation privée ouverte avec un pair : son état propre + son moteur. */
    private class Convo(
        val state: MutableStateFlow<RoomViewState>,
        val engine: RoomConversationEngine,
    )

    /** Conversations privées indexées par `senderId` (deviceId stable du pair). */
    private val convos = HashMap<String, Convo>()

    /** État de la conversation privée ouverte (`null` = vue roster). Suit [GlobalRoomViewState.openPeerId]. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val privateState: StateFlow<RoomViewState?> =
        _state.map { it.openPeerId }.distinctUntilChanged()
            .flatMapLatest { id -> id?.let { convos[it]?.state } ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onPermissionsResult(granted: Boolean) {
        _state.update { it.copy(permissionsGranted = granted) }
    }

    // --- Cycle de vie du maillage -------------------------------------------

    fun start() {
        if (session != null) return
        sessionJob = viewModelScope.launch {
            myDeviceId = settings.ensureDeviceId()
            myPseudo = resolvePseudo()
            val opened = NearbyMeshSession(meshConnector, myDeviceId, myPseudo)
            session = opened
            _state.update { it.copy(active = true) }
            runCatching { opened.events.collect { onEvent(it) } }
        }
    }

    fun stop() {
        sessionJob?.cancel()
        sessionJob = null
        val opened = session
        session = null
        convos.clear()
        viewModelScope.launch { opened?.close() }
        _state.update {
            it.copy(active = false, participants = emptyList(), openPeerId = null, openPeerPseudo = null)
        }
    }

    private suspend fun resolvePseudo(): String =
        settings.settings.first().pseudo.ifBlank { Build.MODEL ?: "" }.ifBlank { "?" }

    // --- Réception -----------------------------------------------------------

    private suspend fun onEvent(event: MeshSessionEvent) {
        when (event) {
            is MeshSessionEvent.RosterChanged ->
                _state.update { it.copy(participants = event.participants) }

            is MeshSessionEvent.Message -> handleMessage(event.envelope)
        }
    }

    private suspend fun handleMessage(envelope: MeshEnvelope) {
        if (envelope.recipientId == null) {
            // Diffusion : seul le chat de groupe est diffusé (les listes/collections sont privées).
            (envelope.body as? RoomMessage.Chat)?.let { chat ->
                _state.update {
                    it.copy(
                        messages = it.messages +
                            GlobalChatLine(envelope.senderPseudo, fromMe = false, chat.text, chat.sentAt),
                    )
                }
            }
            return
        }
        if (envelope.recipientId != myDeviceId) return
        val convo = convos[envelope.senderId] ?: createConvo(envelope.senderId, envelope.senderPseudo)
        convo.engine.handleIncoming(envelope.body)
    }

    // --- Conversation privée -------------------------------------------------

    fun openPrivate(peerId: String) {
        viewModelScope.launch {
            val pseudo = _state.value.participants.firstOrNull { it.id == peerId }?.pseudo ?: "?"
            if (convos[peerId] == null) createConvo(peerId, pseudo)
            _state.update { it.copy(openPeerId = peerId, openPeerPseudo = pseudo) }
        }
    }

    fun closePrivate() {
        _state.update { it.copy(openPeerId = null, openPeerPseudo = null) }
    }

    /** Crée le moteur d'une conversation privée et présente mes listes/collection au pair. */
    private suspend fun createConvo(peerId: String, peerPseudo: String): Convo {
        val convoState = MutableStateFlow(
            RoomViewState(phase = RoomPhase.InRoom, myPseudo = myPseudo, peerPseudo = peerPseudo),
        )
        val engine = RoomConversationEngine(
            scope = viewModelScope,
            state = convoState,
            tradeLists = tradeLists,
            savedTrades = savedTrades,
            collection = collection,
            catalog = catalog,
            send = { session?.sendTo(peerId, it) ?: Unit },
        )
        val init = engine.prepareLocal(myPseudo)
        session?.sendTo(peerId, RoomMessage.CollectionSnapshot(init.entries))
        session?.sendTo(peerId, RoomMessage.TradeLists(init.tradeable, init.wanted))
        return Convo(convoState, engine).also { convos[peerId] = it }
    }

    private fun openEngine(): RoomConversationEngine? =
        _state.value.openPeerId?.let { convos[it]?.engine }

    // --- Actions (chat de groupe) -------------------------------------------

    fun sendGlobalChat(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val sentAt = System.currentTimeMillis()
        _state.update {
            it.copy(messages = it.messages + GlobalChatLine(myPseudo, fromMe = true, trimmed, sentAt))
        }
        viewModelScope.launch { session?.broadcast(RoomMessage.Chat(trimmed, sentAt)) }
    }

    // --- Actions (conversation privée ouverte, déléguées au moteur) ----------

    fun privSetTab(tab: RoomTab) = openEngine()?.setTab(tab) ?: Unit
    fun privSendChat(text: String) = openEngine()?.sendChat(text) ?: Unit
    fun privShareCollection() = openEngine()?.shareCollection() ?: Unit
    fun privShareLists() = openEngine()?.shareLists() ?: Unit

    fun privPropose(iGive: List<MatchLine>, iReceive: List<MatchLine>) =
        openEngine()?.proposeTrade(iGive, iReceive) ?: Unit

    fun privRespondOffer(accept: Boolean) = openEngine()?.respondOffer(accept) ?: Unit
    fun privSaveIncoming() = openEngine()?.saveIncomingForLater() ?: Unit

    fun privSaveComposed(iGive: List<MatchLine>, iReceive: List<MatchLine>) =
        openEngine()?.saveComposedForLater(iGive, iReceive) ?: Unit

    override fun onCleared() {
        super.onCleared()
        session?.let { s -> viewModelScope.launch { s.close() } }
    }
}
