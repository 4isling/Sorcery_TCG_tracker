package com.hayse.sorcery.core.navigation

sealed class Destination(val route: String) {
    data object GameTracker : Destination("game_tracker")
    data object CardBrowser : Destination("card_browser")
    data object Collection : Destination("collection")
}
