package com.hayse.sorcery.feature.social.data.p2p

import com.hayse.sorcery.feature.social.data.p2p.model.PayloadEntry
import com.hayse.sorcery.feature.social.domain.model.SuggestionLine
import com.hayse.sorcery.feature.social.domain.model.SuggestionReason
import com.hayse.sorcery.feature.social.domain.model.SuggestionResult
import com.hayse.sorcery.feature.social.domain.repository.SuggestionCatalog

/**
 * Calcule les échanges suggérés à partir des collections complètes échangées, du point de vue local.
 * Logique pure, testable en JVM.
 *
 * Un côté peut *donner* une carte si elle est en surplus (au-delà du playset de sa rareté) **ou**
 * marquée « à échanger ». L'autre côté en a *besoin* selon une priorité décroissante :
 * `Wanted` (sur sa liste « recherché ») > `CompletesSet` (manquante dans un set qu'il a commencé) >
 * `Missing` (simplement absente). Le raisonnement est *par carte* (tous tirages/finish confondus) ;
 * la ligne proposée référence une impression concrète réellement possédée par le donneur.
 */
object TradeSuggestionComputations {

    fun compute(
        peerPseudo: String?,
        myCollection: List<PayloadEntry>,
        peerCollection: List<PayloadEntry>,
        myTradeable: List<PayloadEntry>,
        myWanted: List<PayloadEntry>,
        peerTradeable: List<PayloadEntry>,
        peerWanted: List<PayloadEntry>,
        catalog: SuggestionCatalog,
    ): SuggestionResult = SuggestionResult(
        peerPseudo = peerPseudo,
        iCanGive = oneDirection(
            giverCollection = myCollection,
            giverTradeable = myTradeable,
            receiverCollection = peerCollection,
            receiverWanted = peerWanted,
            catalog = catalog,
        ),
        iCanReceive = oneDirection(
            giverCollection = peerCollection,
            giverTradeable = peerTradeable,
            receiverCollection = myCollection,
            receiverWanted = myWanted,
            catalog = catalog,
        ),
    )

    /** Cartes que le donneur peut céder et dont le receveur a besoin, référencées par impression. */
    private fun oneDirection(
        giverCollection: List<PayloadEntry>,
        giverTradeable: List<PayloadEntry>,
        receiverCollection: List<PayloadEntry>,
        receiverWanted: List<PayloadEntry>,
        catalog: SuggestionCatalog,
    ): List<SuggestionLine> {
        val giverOwnedByCard = ownedByCard(giverCollection, catalog)
        val receiverOwnedByCard = ownedByCard(receiverCollection, catalog)
        val copiesByCard = copiesByCard(giverCollection, catalog)
        val tradeableByCard = copiesByCard(giverTradeable, catalog)
        val receiverWantedByCard = ownedByCard(receiverWanted, catalog)

        // Sets que le receveur a commencé à collectionner (≥1 carte possédée).
        val receiverStartedSets = catalog.setCardsBySet
            .filterValues { cards -> cards.any { (receiverOwnedByCard[it] ?: 0) > 0 } }
            .keys

        val giveableCards = copiesByCard.keys + tradeableByCard.keys
        return giveableCards.mapNotNull { card ->
            val surplus = surplusOf(card, giverOwnedByCard[card] ?: 0, catalog)
            val tradeableQty = tradeableByCard[card]?.sumOf { it.qty } ?: 0
            val giveable = maxOf(surplus, tradeableQty)
            if (giveable <= 0) return@mapNotNull null

            val (reason, need) = needOf(
                card = card,
                receiverOwned = receiverOwnedByCard[card] ?: 0,
                receiverWantedQty = receiverWantedByCard[card] ?: 0,
                receiverStartedSets = receiverStartedSets,
                catalog = catalog,
            ) ?: return@mapNotNull null

            val copy = pickCopy(card, copiesByCard, tradeableByCard) ?: return@mapNotNull null
            SuggestionLine(
                slug = copy.slug,
                finish = copy.finish,
                quantity = minOf(giveable, need),
                cardName = card,
                reason = reason,
            )
        }.sortedWith(compareBy({ it.reason.ordinal }, { it.cardName }))
    }

    /** Nombre de copies au-delà du playset de la rareté (0 si rareté inconnue ou pas de surplus). */
    private fun surplusOf(card: String, owned: Int, catalog: SuggestionCatalog): Int {
        val max = catalog.rarityByCard[card]?.maxCopies ?: return 0
        return (owned - max).coerceAtLeast(0)
    }

    /** Détermine la raison + la quantité souhaitée ; `null` si le receveur n'a pas besoin de la carte. */
    private fun needOf(
        card: String,
        receiverOwned: Int,
        receiverWantedQty: Int,
        receiverStartedSets: Set<String>,
        catalog: SuggestionCatalog,
    ): Pair<SuggestionReason, Int>? = when {
        receiverWantedQty > 0 -> SuggestionReason.Wanted to receiverWantedQty
        receiverOwned > 0 -> null
        catalog.setsByCard[card].orEmpty().any { it in receiverStartedSets } ->
            SuggestionReason.CompletesSet to 1
        else -> SuggestionReason.Missing to 1
    }

    /** Choisit une impression concrète à proposer : d'abord une possédée, sinon une « à échanger ». */
    private fun pickCopy(
        card: String,
        copiesByCard: Map<String, List<PayloadEntry>>,
        tradeableByCard: Map<String, List<PayloadEntry>>,
    ): PayloadEntry? =
        copiesByCard[card]?.firstOrNull { it.qty > 0 }
            ?: tradeableByCard[card]?.firstOrNull { it.qty > 0 }

    /** Total possédé par carte (somme sur tirages/finish connus du catalogue). */
    private fun ownedByCard(entries: List<PayloadEntry>, catalog: SuggestionCatalog): Map<String, Int> {
        val out = HashMap<String, Int>()
        for (e in entries) {
            if (e.qty <= 0) continue
            val card = catalog.printingToCard[e.slug] ?: continue
            out[card] = (out[card] ?: 0) + e.qty
        }
        return out
    }

    /** Impressions (qty>0) groupées par carte, pour choisir une copie concrète. */
    private fun copiesByCard(entries: List<PayloadEntry>, catalog: SuggestionCatalog): Map<String, List<PayloadEntry>> {
        val out = HashMap<String, MutableList<PayloadEntry>>()
        for (e in entries) {
            if (e.qty <= 0) continue
            val card = catalog.printingToCard[e.slug] ?: continue
            out.getOrPut(card) { mutableListOf() }.add(e)
        }
        return out
    }
}
