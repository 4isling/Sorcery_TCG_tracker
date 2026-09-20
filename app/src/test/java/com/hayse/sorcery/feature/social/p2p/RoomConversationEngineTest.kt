package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.Printing
import com.hayse.sorcery.feature.cards.domain.model.SetEntry
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.ImportReport
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.deck.domain.model.DeckCatalogFilter
import com.hayse.sorcery.feature.deck.domain.model.DeckDetail
import com.hayse.sorcery.feature.deck.domain.model.DeckEntry
import com.hayse.sorcery.feature.deck.domain.model.DeckFormat
import com.hayse.sorcery.feature.deck.domain.model.DeckSummary
import com.hayse.sorcery.feature.deck.domain.repository.DeckRepository
import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.data.p2p.model.RoomMessage
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import com.hayse.sorcery.feature.social.domain.model.SavedTradeItem
import com.hayse.sorcery.feature.social.domain.model.TradeCardLine
import com.hayse.sorcery.feature.social.domain.model.TradeableCopy
import com.hayse.sorcery.feature.social.domain.model.TradeableItem
import com.hayse.sorcery.feature.social.domain.model.WantedCopy
import com.hayse.sorcery.feature.social.domain.model.WantedItem
import com.hayse.sorcery.feature.social.domain.repository.CardCatalog
import com.hayse.sorcery.feature.social.domain.repository.SuggestionCatalog
import com.hayse.sorcery.feature.social.domain.repository.SavedTradeRepository
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import com.hayse.sorcery.feature.social.ui.viewmodel.RoomConversationEngine
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomPhase
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vérifie que le cœur d'échange agnostique au transport ([RoomConversationEngine]) fait bien pivoter
 * la perspective d'une [RoomMessage.TradeOffer] reçue et applique les bons deltas à la collection —
 * comportement identique à celui que portait l'ancien `RoomViewModel`.
 */
class RoomConversationEngineTest {

    private fun CoroutineScope.engine(
        state: MutableStateFlow<RoomViewState>,
        collection: CollectionRepository,
        sent: MutableList<RoomMessage>,
    ) = RoomConversationEngine(
        scope = this,
        state = state,
        tradeLists = EmptyTradeListRepository,
        savedTrades = NoopSavedTradeRepository,
        collection = collection,
        decks = NoopDeckRepository,
        catalog = IdentityCatalog,
        send = { sent += it },
    )

    private suspend fun waitUntil(timeoutMs: Long = 2000, predicate: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!predicate()) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw AssertionError("condition non atteinte dans le délai imparti")
            }
            delay(10)
        }
    }

    @Test
    fun `une offre recue pivote la perspective`() = runBlocking {
        val state = MutableStateFlow(RoomViewState(phase = RoomPhase.InRoom, peerPseudo = "Bob"))
        val engine = engine(state, RecordingCollection(), mutableListOf())

        // Point de vue de Bob : il donne slug-x, il veut slug-y.
        engine.handleIncoming(
            RoomMessage.TradeOffer(
                offerId = "offer-1",
                iGive = listOf(PayloadEntry("slug-x", "standard", 2)),
                iReceive = listOf(PayloadEntry("slug-y", "foil", 1)),
            ),
        )

        val offer = state.value.incomingOffer!!
        assertEquals("offer-1", offer.offerId)
        // Ce que Bob reçoit (slug-y), je le donne ; ce que Bob donne (slug-x), je le reçois.
        assertEquals(listOf(Triple("slug-y", "foil", 1)), offer.iGive.map { it.triple() })
        assertEquals(listOf(Triple("slug-x", "standard", 2)), offer.iReceive.map { it.triple() })
    }

    @Test
    fun `accepter une offre recue applique les bons deltas`() = runBlocking {
        val state = MutableStateFlow(RoomViewState(phase = RoomPhase.InRoom, peerPseudo = "Bob"))
        val collection = RecordingCollection()
        val sent = mutableListOf<RoomMessage>()
        val engine = engine(state, collection, sent)

        engine.handleIncoming(
            RoomMessage.TradeOffer(
                offerId = "offer-1",
                iGive = listOf(PayloadEntry("slug-x", "standard", 2)),
                iReceive = listOf(PayloadEntry("slug-y", "foil", 1)),
            ),
        )
        engine.respondOffer(accept = true)

        waitUntil { collection.adjustments.size == 2 }
        // Je donne slug-y (−1), je reçois slug-x (+2).
        assertEquals(
            listOf(Triple("slug-y", "foil", -1), Triple("slug-x", "standard", 2)),
            collection.adjustments,
        )
        assertTrue(sent.any { it is RoomMessage.TradeResponse && it.offerId == "offer-1" && it.accepted })
    }

    @Test
    fun `l'acceptation d'une offre que j'ai proposee applique ma part`() = runBlocking {
        val state = MutableStateFlow(RoomViewState(phase = RoomPhase.InRoom, peerPseudo = "Bob"))
        val collection = RecordingCollection()
        val sent = mutableListOf<RoomMessage>()
        val engine = engine(state, collection, sent)

        engine.proposeTrade(
            iGive = listOf(MatchLine("slug-a", "standard", 3)),
            iReceive = listOf(MatchLine("slug-b", "foil", 1)),
        )
        waitUntil { sent.any { it is RoomMessage.TradeOffer } }
        val offerId = (sent.first { it is RoomMessage.TradeOffer } as RoomMessage.TradeOffer).offerId

        engine.handleIncoming(RoomMessage.TradeResponse(offerId, accepted = true))

        waitUntil { collection.adjustments.size == 2 }
        assertEquals(
            listOf(Triple("slug-a", "standard", -3), Triple("slug-b", "foil", 1)),
            collection.adjustments,
        )
    }

    @Test
    fun `refuser une offre proposee ne touche pas la collection`() = runBlocking {
        val state = MutableStateFlow(RoomViewState(phase = RoomPhase.InRoom, peerPseudo = "Bob"))
        val collection = RecordingCollection()
        val sent = mutableListOf<RoomMessage>()
        val engine = engine(state, collection, sent)

        engine.proposeTrade(
            iGive = listOf(MatchLine("slug-a", "standard", 3)),
            iReceive = emptyList(),
        )
        waitUntil { sent.any { it is RoomMessage.TradeOffer } }
        val offerId = (sent.first { it is RoomMessage.TradeOffer } as RoomMessage.TradeOffer).offerId

        engine.handleIncoming(RoomMessage.TradeResponse(offerId, accepted = false))
        delay(50)

        assertTrue(collection.adjustments.isEmpty())
    }

    private fun TradeCardLine.triple() = Triple(printing.slug, printing.finish, quantity)
}

/** Résout chaque ligne à l'identique (slug/finish/quantité préservés), sans base de cartes. */
private object IdentityCatalog : CardCatalog {
    override suspend fun resolve(lines: List<MatchLine>): List<TradeCardLine> =
        lines.map { line ->
            TradeCardLine(
                card = Card(
                    name = line.slug,
                    type = "",
                    rarity = null,
                    rulesText = "",
                    cost = null,
                    attack = null,
                    defence = null,
                    life = null,
                    thresholds = emptyMap(),
                    elements = emptyList(),
                    subTypes = emptyList(),
                    imageUri = null,
                ),
                printing = Printing(
                    slug = line.slug,
                    setName = "",
                    releasedAt = "",
                    finish = line.finish,
                    product = "",
                    artist = "",
                    flavorText = "",
                    typeText = "",
                    imageUri = "",
                ),
                quantity = line.quantity,
            )
        }

    override suspend fun suggestionCatalog(): SuggestionCatalog =
        SuggestionCatalog(emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap())
}

/** Enregistre les deltas ; `observeCollection` émet une liste vide pour que `shareCollection` n'échoue pas. */
private class RecordingCollection : CollectionRepository {
    val adjustments = mutableListOf<Triple<String, String, Int>>()

    override suspend fun adjustQuantity(slug: String, finish: String, delta: Int) {
        adjustments += Triple(slug, finish, delta)
    }

    override fun observeCollection(filter: CollectionFilter): Flow<List<SetEntry<CollectionItem>>> =
        MutableStateFlow(emptyList())

    override fun observeOwnedForCard(cardName: String): Flow<List<OwnedCopy>> = emptyFlow()
    override fun observeSetCompletion(): Flow<List<SetCompletion>> = emptyFlow()
    override fun observeSurplus(): Flow<List<SurplusCard>> = emptyFlow()
    override fun observeMissing(setName: String): Flow<List<Card>> = emptyFlow()
    override suspend fun setQuantity(slug: String, finish: String, quantity: Int) = Unit
    override suspend fun importCuriosa(csv: String, replace: Boolean): ImportReport =
        throw NotImplementedError()
}

private object EmptyTradeListRepository : TradeListRepository {
    override fun observeTradeable(): Flow<List<TradeableItem>> = emptyFlow()
    override fun observeWanted(): Flow<List<WantedItem>> = emptyFlow()
    override suspend fun setTradeable(slug: String, finish: String, quantity: Int) = Unit
    override suspend fun setWanted(slug: String, finish: String, quantity: Int) = Unit
    override suspend fun currentTradeableCopies(): List<TradeableCopy> = emptyList()
    override suspend fun currentWantedCopies(): List<WantedCopy> = emptyList()
}

private object NoopSavedTradeRepository : SavedTradeRepository {
    override fun observeSavedTrades(): Flow<List<SavedTradeItem>> = emptyFlow()
    override suspend fun saveTrade(peerPseudo: String, iGive: List<MatchLine>, iReceive: List<MatchLine>) = Unit
    override suspend fun deleteTrade(id: Long) = Unit
    override suspend fun applyTrade(id: Long) = Unit
}

private object NoopDeckRepository : DeckRepository {
    override fun observeDecks(): Flow<List<DeckSummary>> = MutableStateFlow(emptyList())
    override fun observeDeck(deckId: Long): Flow<DeckDetail?> = MutableStateFlow(null)
    override fun observeCatalog(deckId: Long, filter: DeckCatalogFilter): Flow<List<DeckEntry>> = emptyFlow()
    override suspend fun availableTypes(): List<String> = emptyList()
    override suspend fun availableSets(): List<String> = emptyList()
    override suspend fun createDeck(name: String, format: DeckFormat): Long = 0L
    override suspend fun renameDeck(deckId: Long, name: String) = Unit
    override suspend fun deleteDeck(deckId: Long) = Unit
    override suspend fun setCardQuantity(deckId: Long, cardName: String, quantity: Int) = Unit
    override suspend fun adjustCardQuantity(deckId: Long, cardName: String, delta: Int) = Unit
}
