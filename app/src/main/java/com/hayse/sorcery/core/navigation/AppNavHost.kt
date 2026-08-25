package com.hayse.sorcery.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hayse.sorcery.core.ui.composable.PlaceholderScreen
import com.hayse.sorcery.feature.cards.ui.screen.CardDetailScreen
import com.hayse.sorcery.feature.cards.ui.screen.CardListScreen
import com.hayse.sorcery.feature.collection.ui.screen.CollectionScreen
import com.hayse.sorcery.feature.game_tracker.ui.screen.GameTrackerScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.CardBrowser.route,
        modifier = modifier,
    ) {
        composable(Destination.GameTracker.route) {
            GameTrackerScreen()
        }
        composable(Destination.CardBrowser.route) {
            CardListScreen(
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
        composable(
            route = Destination.CardDetail.route,
            arguments = listOf(navArgument(Destination.CardDetail.ARG_NAME) { type = NavType.StringType }),
        ) { entry ->
            val name = entry.arguments?.getString(Destination.CardDetail.ARG_NAME).orEmpty()
            CardDetailScreen(name = name)
        }
        composable(Destination.Collection.route) {
            CollectionScreen(
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
        composable(Destination.DeckBuilder.route) {
            PlaceholderScreen(
                title = "Decks",
                subtitle = "Construction de deck à venir.",
            )
        }
    }
}
