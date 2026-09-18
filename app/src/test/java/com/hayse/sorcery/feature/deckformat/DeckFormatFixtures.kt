package com.hayse.sorcery.feature.deckformat

import com.hayse.sorcery.core.shared.model.Rarity
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.deckformat.domain.model.Deck
import com.hayse.sorcery.feature.deckformat.domain.model.DeckEntry
import com.hayse.sorcery.feature.deckformat.domain.model.DeckZone

fun card(name: String, type: String, rarity: Rarity?): Card = Card(
    name = name,
    type = type,
    rarity = rarity,
    rulesText = "",
    cost = null,
    attack = null,
    defence = null,
    life = null,
    thresholds = emptyMap(),
    elements = emptyList(),
    subTypes = emptyList(),
    imageUri = null,
)

private val referenceSpellbook = mapOf(
    "Bosk Troll" to 4, "Entombed" to 3, "Sherwood Huntress" to 4,
    "Belmotte Longbowmen" to 4, "Cave Trolls" to 4, "Plumed Pegasus" to 4,
    "Midnight Rogue" to 2, "Autumn Unicorn" to 1, "Brown Bears" to 4,
    "Hunting Party" to 2, "Pudge Butcher" to 1, "Amazon Warriors" to 3,
    "Daperyll Vampire" to 1, "Wyvern" to 2, "Bury" to 4, "Lightning Bolt" to 4,
    "Blink" to 4, "Common Sense" to 3, "Buried Alive" to 2, "Cave-In" to 2, "Toolbox" to 2,
)

private val referenceAtlas = mapOf(
    "Common Village" to 4, "Humble Village" to 3, "Rustic Village" to 3, "Simple Village" to 2,
    "Forlorn Keep" to 1, "Vantage Hills" to 1, "Fertile Earth" to 1, "Windmill" to 1,
    "Gothic Tower" to 3, "Dark Tower" to 2, "Lone Tower" to 2, "Open Mausoleum" to 3,
    "Troubled Town" to 4,
)

private val referenceCollection = mapOf(
    "Eltham Townsfolk" to 4, "Penitent Knight" to 1, "Exorcism" to 1, "Mortality" to 1,
    "Dispel" to 1, "Murder of Crows" to 1, "Arc Lightning" to 1,
)

fun referenceDeck(): Deck = Deck(
    avatarName = "Sorcerer",
    entries = buildList {
        referenceSpellbook.forEach { (name, qty) -> add(DeckEntry(name, DeckZone.SPELLBOOK, qty)) }
        referenceAtlas.forEach { (name, qty) -> add(DeckEntry(name, DeckZone.ATLAS, qty)) }
        referenceCollection.forEach { (name, qty) -> add(DeckEntry(name, DeckZone.COLLECTION, qty)) }
    },
)

fun referenceCards(): Map<String, Card> = buildMap {
    referenceSpellbook.keys.forEach { put(it, card(it, "Minion", Rarity.Ordinary)) }
    referenceAtlas.keys.forEach { put(it, card(it, "Site", Rarity.Ordinary)) }
    referenceCollection.keys.forEach { put(it, card(it, "Minion", Rarity.Ordinary)) }
}
