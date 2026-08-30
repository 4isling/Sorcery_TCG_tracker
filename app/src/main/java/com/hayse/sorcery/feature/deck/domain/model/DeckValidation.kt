package com.hayse.sorcery.feature.deck.domain.model

enum class DeckIssueSeverity { Error, Warning }

data class DeckIssue(
    val severity: DeckIssueSeverity,
    val message: String,
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
                issues += DeckIssue(DeckIssueSeverity.Error, "Il faut un Avatar.", section = DeckSection.Avatar)
            avatarCount > format.maxAvatar ->
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    "Un seul Avatar autorisé ($avatarCount sélectionnés).",
                    section = DeckSection.Avatar,
                )
        }

        if (spellbookCount < format.minSpellbook) {
            issues += DeckIssue(
                DeckIssueSeverity.Error,
                "Grimoire : $spellbookCount/${format.minSpellbook} sorts minimum.",
                section = DeckSection.Spellbook,
            )
        }
        if (atlasCount < format.minAtlas) {
            issues += DeckIssue(
                DeckIssueSeverity.Error,
                "Atlas : $atlasCount/${format.minAtlas} sites minimum.",
                section = DeckSection.Atlas,
            )
        }

        entries.forEach { entry ->
            if (deckSectionOf(entry.card.type) == DeckSection.Avatar) return@forEach
            val rarity = entry.card.rarity
            if (rarity != null && rarity in format.bannedRarities) {
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    "${entry.card.name} : rareté ${rarity.name} interdite dans ce format.",
                    cardName = entry.card.name,
                )
            }
            val limit = format.copyLimit(rarity)
            if (entry.quantity > limit) {
                issues += DeckIssue(
                    DeckIssueSeverity.Error,
                    "${entry.card.name} : ${entry.quantity} copies (max $limit).",
                    cardName = entry.card.name,
                )
            }
        }

        return DeckValidation(issues)
    }
}
