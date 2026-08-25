package com.hayse.sorcery.core.navigation

sealed class Destination(val route: String, val label: String) {
    data object GameTracker : Destination("game_tracker", "Suivi de partie")
    data object CardBrowser : Destination("card_browser", "Cartes")
    data object Collection : Destination("collection", "Collection")
    data object DeckBuilder : Destination("deck_builder", "Decks")

    /** Détail d'une carte, hors drawer. Argument : `name`. */
    data object CardDetail : Destination("card_detail/{name}", "Carte") {
        const val ARG_NAME = "name"
        fun routeFor(name: String): String = "card_detail/${android.net.Uri.encode(name)}"
    }

    companion object {
        /**
         * Entrées affichées dans le drawer, dans l'ordre.
         *
         * Getter (et non `val`) volontairement : une `val` serait évaluée pendant le
         * `<clinit>` du companion, avant que les `data object` imbriqués aient leur
         * INSTANCE assignée → liste avec des éléments `null` (NPE au runtime).
         */
        val drawerDestinations: List<Destination>
            get() = listOf(CardBrowser, Collection, GameTracker, DeckBuilder)
    }
}