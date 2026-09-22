package com.hayse.sorcery.feature.deck.domain.model

import com.hayse.sorcery.core.shared.model.Rarity

/**
 * Règles de construction d'un format, entièrement pilotées par les données : ajouter un format
 * revient à déclarer une nouvelle instance dans [all], sans toucher au validateur.
 *
 * - [minSpellbook] / [minAtlas] : tailles minimales du grimoire (sorts) et de l'atlas (sites).
 * - [maxCollection] : taille maximale de la Collection (réserve de sorts/sites, 10 en Sorcery).
 *   Les copies en Collection comptent avec celles du deck principal dans la limite par rareté.
 * - [maxAvatar] : nombre d'Avatars autorisés (Sorcery : exactement 1).
 * - [bannedRarities] : raretés interdites dans ce format (ex. un format sans Unique).
 * - [copyLimitOverride] : plafond de copies commun s'il remplace les limites par rareté (sinon null).
 */
data class DeckFormat(
    val id: String,
    val label: String,
    val minSpellbook: Int,
    val minAtlas: Int,
    val maxCollection: Int,
    val maxAvatar: Int,
    val bannedRarities: Set<Rarity>,
    val copyLimitOverride: Int?,
) {
    /** Nombre maximal de copies autorisées pour une carte de cette rareté. */
    fun copyLimit(rarity: Rarity?): Int =
        copyLimitOverride ?: rarity?.maxCopies ?: DEFAULT_COPY_LIMIT

    companion object {
        const val DEFAULT_COPY_LIMIT = 4

        val Constructed = DeckFormat(
            id = "constructed",
            label = "Constructed",
            minSpellbook = 60,
            minAtlas = 30,
            maxCollection = 10,
            maxAvatar = 1,
            bannedRarities = emptySet(),
            copyLimitOverride = null,
        )

        val Poorcery = Constructed.copy(
            id = "poorcery",
            label = "Poorcery",
            bannedRarities = setOf(Rarity.Elite, Rarity.Unique),
        )

        val all: List<DeckFormat> = listOf(Constructed, Poorcery)

        fun fromId(id: String): DeckFormat = all.firstOrNull { it.id == id } ?: Constructed
    }
}
