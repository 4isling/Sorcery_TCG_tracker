package com.hayse.sorcery.feature.social.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.data.p2p.model.SCHEMA_VERSION
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.p2p.PairingRole
import com.hayse.sorcery.feature.social.domain.p2p.RoomConnector
import com.hayse.sorcery.feature.social.domain.p2p.RoomSession
import com.hayse.sorcery.feature.social.domain.p2p.PairingBootstrap
import com.hayse.sorcery.feature.social.domain.p2p.SessionToken
import com.hayse.sorcery.feature.social.domain.repository.CardCatalog
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomFailure
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomPhase
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomTab
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pilote la room 1:1 : garde toute la phase d'appairage (rôle, QR, NFC, code, permissions) puis,
 * une fois connecté, tient une session persistante (chat, collections, offres) au-dessus d'un
 * [RoomSession]. Remplace l'ancien `PairingViewModel` one-shot.
*/
class RoomViewModel(
    private val bootstrap: PairingBootstrap,
    private val connector: RoomConnector,
    private val tradeLists: TradeListRepository,
    private val savedTrades: SavedTradeRepository,
    private val collection: CollectionRepository,
    private val catalog: CardCatalog,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RoomViewState())
    val state: StateFlow<RoomViewState> = _state.asStateFlow()

    private var session: RoomSession? = null
    private var sessionJob: Job? = null

    /** Moteur de conversation (chat, collections, offres) créé à l'ouverture de la session. */
    private var engine: RoomConversationEngine? = null

    // --- Phase d'appairage ---------------------------------------------------

    fun chooseHost() {
        val token = bootstrap.newToken()
        _state.update {
            it.copy(
                role = PairingRole.HOST,
                hostCode = token.sessionId,
                hostQrPayload = bootstrap.encodeToken(token),
            )
        }
    }

    fun chooseGuest() {
        _state.update { it.copy(role = PairingRole.GUEST) }
    }

    fun onGuestCodeChange(code: String) {
        _state.update { it.copy(guestCodeInput = code.uppercase()) }
    }

    fun openScanner() {
        _state.update { it.copy(scanning = true) }
    }

    fun closeScanner() {
        _state.update { it.copy(scanning = false) }
    }

    /** QR scanné : décode le token et remplit le champ code, sinon ignore (frame invalide). */
    fun onQrScanned(raw: String) {
        if (!_state.value.scanning) return
        val token = bootstrap.decodeToken(raw) ?: return
        _state.update { it.copy(guestCodeInput = token.sessionId, scanning = false) }
    }

    /** Token lu par NFC (invité) : décode et remplit le champ code, sinon ignore. */
    fun onNfcToken(raw: String) {
        val token = bootstrap.decodeToken(raw) ?: return
        _state.update { it.copy(guestCodeInput = token.sessionId) }
    }

    fun onPermissionsResult(granted: Boolean) {
        _state.update { it.copy(permissionsGranted = granted) }
    }

    // --- Ouverture de la room ------------------------------------------------

    fun startSession() {
        val current = _state.value
        val role = current.role ?: return
        val code = when (role) {
            PairingRole.HOST -> current.hostCode
            PairingRole.GUEST -> current.guestCodeInput.trim()
        }?.takeIf { it.isNotEmpty() } ?: return

        val token = SessionToken(code, SCHEMA_VERSION)
        sessionJob?.cancel()
        _state.update { it.copy(phase = RoomPhase.Connecting, failure = null) }
        sessionJob = viewModelScope.launch {
            val pseudo = resolvePseudo()

            val opened = runCatching { connector.connect(role, token) }.getOrNull()
            if (opened == null) {
                _state.update { it.copy(phase = RoomPhase.Failed, failure = RoomFailure.TRANSPORT) }
                return@launch
            }
            session = opened
            val engine = RoomConversationEngine(
                scope = viewModelScope,
                state = _state,
                tradeLists = tradeLists,
                savedTrades = savedTrades,
                collection = collection,
                catalog = catalog,
                send = { opened.send(it) },
            )
            this@RoomViewModel.engine = engine

            val init = engine.prepareLocal(pseudo)
            opened.send(RoomMessage.Hello(pseudo))
            opened.send(RoomMessage.CollectionSnapshot(init.entries))
            opened.send(RoomMessage.TradeLists(init.tradeable, init.wanted))
            _state.update { it.copy(phase = RoomPhase.InRoom, myPseudo = pseudo) }

            runCatching {
                opened.incoming.collect { handleIncoming(engine, it) }
            }
        }
    }

    private suspend fun resolvePseudo(): String =
        settings.settings.first().pseudo.ifBlank { Build.MODEL ?: "" }.ifBlank { "?" }

    /** Le contrôle de version du `Hello` reste ici ; tout le reste part au moteur. */
    private suspend fun handleIncoming(engine: RoomConversationEngine, message: RoomMessage) {
        if (message is RoomMessage.Hello && message.schemaVersion > SCHEMA_VERSION) {
            session?.close()
            session = null
            _state.update { it.copy(phase = RoomPhase.Failed, failure = RoomFailure.INCOMPATIBLE_VERSION) }
            return
        }
        if (message is RoomMessage.Hello) {
            _state.update { it.copy(peerPseudo = message.pseudo) }
            return
        }
        engine.handleIncoming(message)
    }

    // --- Actions dans la room (déléguées au moteur) --------------------------

    fun setTab(tab: RoomTab) = engine?.setTab(tab) ?: Unit

    fun sendChat(text: String) = engine?.sendChat(text) ?: Unit

    fun shareCollection() = engine?.shareCollection() ?: Unit

    fun shareLists() = engine?.shareLists() ?: Unit

    fun proposeTrade(iGive: List<MatchLine>, iReceive: List<MatchLine>) =
        engine?.proposeTrade(iGive, iReceive) ?: Unit

    fun respondOffer(accept: Boolean) = engine?.respondOffer(accept) ?: Unit

    fun saveIncomingForLater() = engine?.saveIncomingForLater() ?: Unit

    fun saveComposedForLater(iGive: List<MatchLine>, iReceive: List<MatchLine>) =
        engine?.saveComposedForLater(iGive, iReceive) ?: Unit

    fun leaveRoom() {
        sessionJob?.cancel()
        sessionJob = null
        viewModelScope.launch { session?.close() }
        session = null
        engine?.reset()
        engine = null
        _state.value = RoomViewState(permissionsGranted = _state.value.permissionsGranted)
    }

    override fun onCleared() {
        super.onCleared()
        session?.let { s -> viewModelScope.launch { s.close() } }
    }
}
