package com.hayse.sorcery.core.ui.theme

/** Sets Sorcery reconnus par le thème contextuel. Le contenu consulté teinte l'écran. */
enum class SorcerySet {
    Alpha,
    Beta,
    ArthurianLegends,
    Gothic,
    Dragonlord,
    Promotional,
}

/** Associe un nom de set (tel qu'il apparaît dans les cartes) à un [SorcerySet]. */
fun sorcerySetFromName(setName: String?): SorcerySet? = when (setName?.trim()?.lowercase()) {
    "alpha" -> SorcerySet.Alpha
    "beta" -> SorcerySet.Beta
    "arthurian legends", "arthurian" -> SorcerySet.ArthurianLegends
    "gothic" -> SorcerySet.Gothic
    "dragonlord", "dragonlords" -> SorcerySet.Dragonlord
    "promotional", "promo" -> SorcerySet.Promotional
    else -> null
}
