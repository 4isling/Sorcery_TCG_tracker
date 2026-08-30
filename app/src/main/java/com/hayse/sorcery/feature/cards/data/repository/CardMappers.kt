package com.hayse.sorcery.feature.cards.data.repository

import com.hayse.sorcery.core.shared.model.Element
import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.data.local.entity.CardEntity
import com.hayse.sorcery.feature.cards.data.local.entity.CardWithPrintings
import com.hayse.sorcery.feature.cards.data.local.entity.PrintingEntity
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardDetail
import com.hayse.sorcery.feature.cards.domain.model.Printing

private const val IMAGE_BASE = "file:///android_asset/cards/images"

private fun imageUri(slug: String): String = "$IMAGE_BASE/$slug.webp"

private fun parseElements(raw: String): List<Element> =
    raw.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { token -> Element.entries.firstOrNull { it.name.equals(token, ignoreCase = true) } }

private fun parseSubTypes(raw: String): List<String> =
    raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

private fun rarityOf(raw: String): Rarity? =
    Rarity.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }

private fun CardEntity.toCard(imageUri: String?): Card = Card(
    name = name,
    type = type,
    rarity = rarityOf(rarity),
    rulesText = rulesText,
    cost = cost,
    attack = attack,
    defence = defence,
    life = life,
    thresholds = mapOf(
        Element.Air to thAir,
        Element.Earth to thEarth,
        Element.Fire to thFire,
        Element.Water to thWater,
    ),
    elements = parseElements(elements),
    subTypes = parseSubTypes(subTypes),
    imageUri = imageUri,
)

fun CardWithPrintings.toCard(imageUriForSlugs: (List<String>) -> String? = { null }): Card =
    card.toCard(imageUri = imageUriForSlugs(printings.map { it.slug }))

fun PrintingEntity.toDomain(): Printing = Printing(
    slug = slug,
    setName = setName,
    releasedAt = releasedAt,
    finish = finish,
    product = product,
    artist = artist,
    flavorText = flavorText,
    typeText = typeText,
    imageUri = imageUri(slug),
)

fun CardWithPrintings.toDetail(imageUriForSlugs: (List<String>) -> String? = { null }): CardDetail = CardDetail(
    card = toCard(imageUriForSlugs),
    printings = printings.map { it.toDomain() },
)
