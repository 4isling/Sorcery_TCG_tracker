package com.hayse.sorcery.feature.cards.data.local

import android.content.Context
import com.hayse.sorcery.feature.cards.data.local.dao.CardDao
import com.hayse.sorcery.feature.cards.data.local.entity.CardEntity
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity
import com.hayse.sorcery.feature.cards.data.local.model.CardCatalogJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Remplit Room à partir de `assets/cards/cards.json` au premier lancement.
 * Idempotent (ne fait rien si la base contient déjà des cartes) et tolérant à l'absence du fichier
 * (l'app démarre avec un catalogue vide tant que les données ne sont pas générées hors-app).
 */
class CardCatalogSeeder(
    private val context: Context,
    private val dao: CardDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.count() > 0) return@withContext

        val raw = runCatching { context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() } }
            .getOrNull() ?: return@withContext

        val catalog = runCatching { json.decodeFromString<CardCatalogJson>(raw) }
            .getOrNull() ?: return@withContext

        val cards = catalog.cards.map { c ->
            CardEntity(
                name = c.name,
                type = c.type,
                rarity = c.rarity,
                rulesText = c.rulesText,
                cost = c.cost,
                attack = c.attack,
                defence = c.defence,
                life = c.life,
                thAir = c.thresholds.air,
                thEarth = c.thresholds.earth,
                thFire = c.thresholds.fire,
                thWater = c.thresholds.water,
                elements = c.elements,
                subTypes = c.subTypes,
            )
        }
        val printings = catalog.cards.flatMap { c ->
            c.printings.map { p ->
                PrintingEntity(
                    slug = p.slug,
                    cardName = c.name,
                    setName = p.setName,
                    releasedAt = p.releasedAt,
                    finish = p.finish,
                    product = p.product,
                    artist = p.artist,
                    flavorText = p.flavorText,
                    typeText = p.typeText,
                    source = SOURCE_API,
                )
            }
        }

        dao.insertCards(cards)
        dao.insertPrintings(printings)
    }

    companion object {
        private const val ASSET_PATH = "cards/cards.json"
        private const val SOURCE_API = "API"
    }
}
