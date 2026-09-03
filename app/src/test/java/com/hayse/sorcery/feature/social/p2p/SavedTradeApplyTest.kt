package com.hayse.sorcery.feature.social.p2p

import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.model.PrintingKey
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.collection.domain.model.CollectionFilter
import com.hayse.sorcery.feature.collection.domain.model.CollectionItem
import com.hayse.sorcery.feature.collection.domain.model.ImportReport
import com.hayse.sorcery.feature.collection.domain.model.OwnedCopy
import com.hayse.sorcery.feature.collection.domain.model.SetCompletion
import com.hayse.sorcery.feature.collection.domain.model.SurplusCard
import com.hayse.sorcery.feature.collection.domain.repository.CollectionRepository
import com.hayse.sorcery.feature.social.data.local.entity.SavedTradeEntity
import com.hayse.sorcery.feature.social.data.local.entity.TradeableEntryEntity
import com.hayse.sorcery.feature.social.data.local.entity.WantedEntryEntity
import com.hayse.sorcery.feature.social.data.local.dao.SocialDao
import com.hayse.sorcery.feature.social.data.repository.SavedTradeRepositoryImpl
import com.hayse.sorcery.feature.social.domain.model.MatchLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedTradeApplyTest {

    @Test
    fun `applyTrade applique les deltas et marque l'echange fait`() = runBlocking {
        val socialDao = FakeSocialDao()
        val collection = RecordingCollectionRepository()
        val repository = SavedTradeRepositoryImpl(
            socialDao = socialDao,
            cardDao = ThrowingCardDao(),
            imageUri = { null },
            collectionRepository = collection,
        )

        repository.saveTrade(
            peerPseudo = "Alice",
            iGive = listOf(
                MatchLine(slug = "slug-a", finish = "standard", quantity = 2),
                MatchLine(slug = "slug-b", finish = "foil", quantity = 1),
            ),
            iReceive = listOf(
                MatchLine(slug = "slug-c", finish = "standard", quantity = 3),
            ),
        )

        val id = socialDao.lastInsertedId
        repository.applyTrade(id)

        assertEquals(
            listOf(
                Triple("slug-a", "standard", -2),
                Triple("slug-b", "foil", -1),
                Triple("slug-c", "standard", 3),
            ),
            collection.adjustments,
        )
        assertTrue(socialDao.trades.getValue(id).done)
    }

    @Test
    fun `applyTrade sur un id inconnu ne fait rien`() = runBlocking {
        val socialDao = FakeSocialDao()
        val collection = RecordingCollectionRepository()
        val repository = SavedTradeRepositoryImpl(
            socialDao = socialDao,
            cardDao = ThrowingCardDao(),
            imageUri = { null },
            collectionRepository = collection,
        )

        repository.applyTrade(999L)

        assertTrue(collection.adjustments.isEmpty())
    }
}

private class FakeSocialDao : SocialDao {
    val trades = HashMap<Long, SavedTradeEntity>()
    private var nextId = 1L
    var lastInsertedId = 0L
        private set

    override suspend fun insertSavedTrade(entry: SavedTradeEntity): Long {
        val id = nextId++
        trades[id] = entry.copy(id = id)
        lastInsertedId = id
        return id
    }

    override suspend fun getSavedTrade(id: Long): SavedTradeEntity? = trades[id]

    override suspend fun setSavedTradeDone(id: Long, done: Boolean) {
        trades[id]?.let { trades[id] = it.copy(done = done) }
    }

    override suspend fun deleteSavedTrade(id: Long) { trades.remove(id) }

    override fun observeSavedTrades(): Flow<List<SavedTradeEntity>> =
        MutableStateFlow(trades.values.toList())

    override fun observeTradeable(): Flow<List<TradeableEntryEntity>> = emptyFlow()
    override fun observeWanted(): Flow<List<WantedEntryEntity>> = emptyFlow()
    override suspend fun getTradeable(): List<TradeableEntryEntity> = emptyList()
    override suspend fun getWanted(): List<WantedEntryEntity> = emptyList()
    override suspend fun upsertTradeable(entry: TradeableEntryEntity) = Unit
    override suspend fun upsertWanted(entry: WantedEntryEntity) = Unit
    override suspend fun deleteTradeable(slug: String, finish: String) = Unit
    override suspend fun deleteWanted(slug: String, finish: String) = Unit
    override suspend fun clearTradeable() = Unit
    override suspend fun clearWanted() = Unit
}

private class RecordingCollectionRepository : CollectionRepository {
    val adjustments = mutableListOf<Triple<String, String, Int>>()

    override suspend fun adjustQuantity(slug: String, finish: String, delta: Int) {
        adjustments += Triple(slug, finish, delta)
    }

    override fun observeCollection(filter: CollectionFilter): Flow<List<CollectionItem>> = emptyFlow()
    override fun observeOwnedForCard(cardName: String): Flow<List<OwnedCopy>> = emptyFlow()
    override fun observeSetCompletion(): Flow<List<SetCompletion>> = emptyFlow()
    override fun observeSurplus(): Flow<List<SurplusCard>> = emptyFlow()
    override fun observeMissing(setName: String): Flow<List<Card>> = emptyFlow()
    override suspend fun setQuantity(slug: String, finish: String, quantity: Int) = Unit
    override suspend fun importCuriosa(csv: String, replace: Boolean): ImportReport =
        throw NotImplementedError()
}

private class ThrowingCardDao : CardDao {
    override suspend fun count(): Int = throw NotImplementedError()
    override suspend fun insertCards(cards: List<com.hayse.sorcery.feature.cards.data.local.entity.CardEntity>) =
        throw NotImplementedError()
    override suspend fun insertPrintings(printings: List<com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity>) =
        throw NotImplementedError()
    override fun observeCards(
        query: String?,
        element: String?,
        type: String?,
        rarity: String?,
        setName: String?,
    ): Flow<List<CardWithPrintings>> = throw NotImplementedError()
    override suspend fun getCardWithPrintings(name: String): CardWithPrintings? = throw NotImplementedError()
    override suspend fun distinctTypes(): List<String> = throw NotImplementedError()
    override suspend fun distinctSets(): List<String> = throw NotImplementedError()
    override suspend fun allPrintingKeys(): List<PrintingKey> = throw NotImplementedError()
}
