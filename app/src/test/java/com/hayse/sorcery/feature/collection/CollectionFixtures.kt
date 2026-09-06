package com.hayse.sorcery.feature.collection

import com.hayse.sorcery.feature.cards.data.local.entity.CardEntity
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.CollectionEntryEntity
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity
import com.hayse.sorcery.feature.cards.data.local.model.PrintingKey

/** Fabriques de test pour la logique de collection (JVM, sans Room). */
object CollectionFixtures {

    fun card(
        name: String,
        rarity: String = "Ordinary",
        type: String = "Minion",
        elements: String = "",
    ): CardEntity = CardEntity(
        name = name,
        type = type,
        rarity = rarity,
        rulesText = "",
        cost = null,
        attack = null,
        defence = null,
        life = null,
        thAir = 0,
        thEarth = 0,
        thFire = 0,
        thWater = 0,
        elements = elements,
        subTypes = "",
    )

    fun printing(
        slug: String,
        cardName: String,
        setName: String = "Alpha",
        finish: String = "Standard",
        product: String = "Booster",
    ): PrintingEntity = PrintingEntity(
        slug = slug,
        cardName = cardName,
        setName = setName,
        releasedAt = "",
        finish = finish,
        product = product,
        artist = "",
        flavorText = "",
        typeText = "",
        source = "",
    )

    fun cardWith(
        name: String,
        rarity: String = "Ordinary",
        type: String = "Minion",
        elements: String = "",
        printings: List<PrintingEntity>,
    ): CardWithPrintings = CardWithPrintings(card(name, rarity, type, elements), printings)

    fun key(
        slug: String,
        cardName: String,
        setName: String = "Alpha",
        finish: String = "Standard",
        product: String = "Booster",
    ): PrintingKey = PrintingKey(slug, cardName, setName, finish, product)

    fun entry(slug: String, finish: String = "Standard", quantity: Int): CollectionEntryEntity =
        CollectionEntryEntity(slug, finish, quantity)
}
