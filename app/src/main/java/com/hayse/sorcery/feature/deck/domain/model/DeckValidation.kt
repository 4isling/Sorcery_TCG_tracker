package com.hayse.sorcery.feature.deck.domain.model

enum class DeckIssueSeverity { Error, Warning }

/**
 * Problème de validation, indépendant de la langue. Le rendu texte est fait côté UI
 * (voir `DeckIssueMessage.text()` dans la couche ui) pour rester localisable.
 */
sealed interface DeckIssueMessage {
    data object AvatarRequired : DeckIssueMessage
    data class TooManyAvatars(val count: Int) : DeckIssueMessage
    data class SpellbookTooSmall(val count: Int, val min: Int) : DeckIssueMessage
    data class AtlasTooSmall(val count: Int, val min: Int) : DeckIssueMessage
    data class CollectionTooLarge(val count: Int, val max: Int) : DeckIssueMessage
    data class BannedRarity(val cardName: String, val rarity: String) : DeckIssueMessage
    data class TooManyCopies(val cardName: String, val quantity: Int, val limit: Int) : DeckIssueMessage
}

data class DeckIssue(
    val severity: DeckIssueSeverity,
    val message: DeckIssueMessage,
    /** Section à ouvrir dans l'onglet Ajouter pour corriger le problème. */
    val section: DeckSection? = null,
    /** Carte précise concernée (limite de copies, rareté interdite). */
    val cardName: String? = null,
) {
    val isFixable: Boolean get() = section != null || cardName != null
}

data class DeckValidation(
    val issues: List<DeckIssue>,
) {
    val isLegal: Boolean get() = issues.none { it.severity == DeckIssueSeverity.Error }
}

/**
 * Confronte le contenu d'un deck aux règles de son format. Les entrées portent leur zone
 * ([DeckEntry.section]) : le deck principal a des minimums, la Collection un maximum, et la
 * limite de copies par rareté s'applique à la somme des zones pour une même carte.
 */
object DeckValidator {

    fun validate(format: DeckFormat, entries: List<DeckEntry>): DeckValidation {
        val issues = mutableListOf<DeckIssue>()

        fun countIn(section: DeckSection) = entries.filter { it.section == section }.sumOf { it.quantity }
        val avatarCount = countIn(DeckSection.Avatar)
        val spellbookCount = countIn(DeckSection.Spellbook)
        val atlasCount = countIn(DeckSection.Atlas)
        val collectionCount = countIn(DeckSection.Collection)

        when {
            avatarCount == 0 ->
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    DeckIssueMessage.AvatarRequired,
                    section = DeckSection.Avatar,
                )
            avatarCount > format.maxAvatar ->
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    DeckIssueMessage.TooManyAvatars(avatarCount),
                    section = DeckSection.Avatar,
                )
        }

        if (spellbookCount < format.minSpellbook) {
            issues += DeckIssue(
                DeckIssueSeverity.Error,
                DeckIssueMessage.SpellbookTooSmall(spellbookCount, format.minSpellbook),
                section = DeckSection.Spellbook,
            )
        }
        if (atlasCount < format.minAtlas) {
            issues += DeckIssue(
                DeckIssueSeverity.Error,
                DeckIssueMessage.AtlasTooSmall(atlasCount, format.minAtlas),
                section = DeckSection.Atlas,
            )
        }
        if (collectionCount > format.maxCollection) {
            issues += DeckIssue(
                DeckIssueSeverity.Error,
                DeckIssueMessage.CollectionTooLarge(collectionCount, format.maxCollection),
                section = DeckSection.Collection,
            )
        }

        entries
            .filter { it.section != DeckSection.Avatar }
            .groupBy { it.card.name }
            .forEach { (cardName, lines) ->
                val card = lines.first().card
                val rarity = card.rarity
                if (rarity != null && rarity in format.bannedRarities) {
                    issues += DeckIssue(
                        DeckIssueSeverity.Error,
                        DeckIssueMessage.BannedRarity(cardName, rarity.name),
                        cardName = cardName,
                    )
                }
                val limit = format.copyLimit(rarity)
                val copies = lines.sumOf { it.quantity }
                if (copies > limit) {
                    issues += DeckIssue(
                        DeckIssueSeverity.Error,
                        DeckIssueMessage.TooManyCopies(cardName, copies, limit),
                        cardName = cardName,
                    )
                }
            }

        return DeckValidation(issues)
    }
}
