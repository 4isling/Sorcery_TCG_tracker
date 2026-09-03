package com.hayse.sorcery.core.navigation

import androidx.annotation.StringRes
import com.hayse.sorcery.R

sealed class Destination(val route: String, @StringRes val labelRes: Int) {
    data object Home : Destination("home", R.string.nav_home)
    data object GameMenu : Destination("game_menu", R.string.nav_game_menu)
    data object GameSetup : Destination("game_setup", R.string.nav_game_setup)
    data object GameTracker : Destination("game_tracker", R.string.nav_game_tracker)
    data object GameHistory : Destination("game_history", R.string.nav_game_history)
    data object CardBrowser : Destination("card_browser", R.string.nav_cards)
    data object Collection : Destination("collection", R.string.nav_collection)
    data object DeckBuilder : Destination("deck_builder", R.string.nav_decks)
    data object Social : Destination("social", R.string.nav_social)
    data object Settings : Destination("settings", R.string.nav_settings)

    /** Détail d'une carte, hors drawer. Argument : `name`. */
    data object CardDetail : Destination("card_detail/{name}", R.string.nav_card_detail) {
        const val ARG_NAME = "name"
        fun routeFor(name: String): String = "card_detail/${android.net.Uri.encode(name)}"
    }

    /** Éditeur d'un deck, hors drawer. Argument : `deckId`. */
    data object DeckEditor : Destination("deck_editor/{deckId}", R.string.nav_deck_editor) {
        const val ARG_DECK_ID = "deckId"
        fun routeFor(deckId: Long): String = "deck_editor/$deckId"
    }

    /** Room d'échange interactive 1:1, hors drawer. */
    data object Room : Destination("room", R.string.nav_room)

    /** Salon « Global » maillé décentralisé, hors drawer. */
    data object Global : Destination("global", R.string.nav_global)

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
            get() = listOf(Home, CardBrowser, Collection, GameMenu, DeckBuilder, Social, Settings)

        private val allDestinations: List<Destination>
            get() = listOf(
                Home, GameMenu, GameSetup, GameTracker, GameHistory,
                CardBrowser, Collection, DeckBuilder, Social, Room, Global, Settings,
            )

        /** Titre de la barre supérieure pour une route donnée. */
        @StringRes
        fun labelForRoute(route: String?): Int = when {
            route == null -> R.string.nav_app_default
            route.startsWith("card_detail") -> CardDetail.labelRes
            route.startsWith("deck_editor") -> DeckEditor.labelRes
            else -> allDestinations.firstOrNull { it.route == route }?.labelRes ?: R.string.nav_app_default
        }
    }
}