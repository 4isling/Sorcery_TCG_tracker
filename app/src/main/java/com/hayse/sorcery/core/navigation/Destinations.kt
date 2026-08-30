package com.hayse.sorcery.core.navigation

sealed class Destination(val route: String, val label: String) {
    data object Home : Destination("home", "Accueil")
    data object GameMenu : Destination("game_menu", "Suivi de partie")
    data object GameSetup : Destination("game_setup", "Nouvelle partie")
    data object GameTracker : Destination("game_tracker", "Partie en cours")
    data object GameHistory : Destination("game_history", "Historique des parties")
    data object CardBrowser : Destination("card_browser", "Cartes")
    data object Collection : Destination("collection", "Collection")
    data object DeckBuilder : Destination("deck_builder", "Decks")
    data object Settings : Destination("settings", "Paramètres")

    /** Détail d'une carte, hors drawer. Argument : `name`. */
    data object CardDetail : Destination("card_detail/{name}", "Carte") {
        const val ARG_NAME = "name"
        fun routeFor(name: String): String = "card_detail/${android.net.Uri.encode(name)}"
    }

    /** Éditeur d'un deck, hors drawer. Argument : `deckId`. */
    data object DeckEditor : Destination("deck_editor/{deckId}", "Deck") {
        const val ARG_DECK_ID = "deckId"
        fun routeFor(deckId: Long): String = "deck_editor/$deckId"
    }

    companion object {
        /** Sous-graphe regroupant le flux de partie (menu → setup → partie). */
        const val GAME_GRAPH_ROUTE = "game_graph"

        /**
         * Entrées affichées dans le drawer, dans l'ordre.
         *
         * Getter (et non `val`) volontairement : une `val` serait évaluée pendant le
         * `<clinit>` du companion, avant que les `data object` imbriqués aient leur
         * INSTANCE assignée → liste avec des éléments `null` (NPE au runtime).
         */
        val drawerDestinations: List<Destination>
            get() = listOf(Home, CardBrowser, Collection, GameMenu, DeckBuilder, Settings)

        private val allDestinations: List<Destination>
            get() = listOf(
                Home, GameMenu, GameSetup, GameTracker, GameHistory,
                CardBrowser, Collection, DeckBuilder, Settings,
            )

        /** Titre de la barre supérieure pour une route donnée. */
        fun labelForRoute(route: String?): String = when {
            route == null -> "Sorcery"
            route.startsWith("card_detail") -> CardDetail.label
            route.startsWith("deck_editor") -> DeckEditor.label
            else -> allDestinations.firstOrNull { it.route == route }?.label ?: "Sorcery"
        }
    }
}