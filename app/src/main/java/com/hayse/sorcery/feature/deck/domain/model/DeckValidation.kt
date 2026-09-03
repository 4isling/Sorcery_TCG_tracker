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

/** Confronte le contenu d'un deck aux règles de son format. */
object DeckValidator {

    fun validate(format: DeckFormat, entries: List<DeckEntry>): DeckValidation {
        val issues = mutableListOf<DeckIssue>()

        val avatarCount = entries.filter { deckSectionOf(it.card.type) == DeckSection.Avatar }.sumOf { it.quantity }
        val spellbookCount = entries.filter { deckSectionOf(it.card.type) == DeckSection.Spellbook }.sumOf { it.quantity }
        val atlasCount = entries.filter { deckSectionOf(it.card.type) == DeckSection.Atlas }.sumOf { it.quantity }

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

        entries.forEach { entry ->
            if (deckSectionOf(entry.card.type) == DeckSection.Avatar) return@forEach
            val rarity = entry.card.rarity
            if (rarity != null && rarity in format.bannedRarities) {
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    DeckIssueMessage.BannedRarity(entry.card.name, rarity.name),
                    cardName = entry.card.name,
                )
            }
            val limit = format.copyLimit(rarity)
            if (entry.quantity > limit) {
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    DeckIssueMessage.TooManyCopies(entry.card.name, entry.quantity, limit),
                    cardName = entry.card.name,
                )
            }
        }

        return DeckValidation(issues)
    }
}
