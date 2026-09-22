package com.hayse.sorcery.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.LocalWindowWidthSizeClass
import com.hayse.sorcery.core.ui.composable.EmptyState
import com.hayse.sorcery.core.ui.isExpanded
import com.hayse.sorcery.feature.cards.ui.screen.CardDetailPager
import com.hayse.sorcery.feature.cards.ui.screen.CardDetailScreen
import com.hayse.sorcery.feature.cards.ui.screen.CardListScreen
import com.hayse.sorcery.feature.collection.ui.screen.CollectionScreen
import com.hayse.sorcery.feature.deck.ui.screen.DeckEditorScreen
import com.hayse.sorcery.feature.deck.ui.screen.DeckListScreen
import com.hayse.sorcery.feature.game_tracker.ui.screen.GameHistoryScreen
import com.hayse.sorcery.feature.game_tracker.ui.screen.GameMenuScreen
import com.hayse.sorcery.feature.game_tracker.ui.screen.GameSetupScreen
import com.hayse.sorcery.feature.game_tracker.ui.screen.GameTrackerScreen
import com.hayse.sorcery.feature.game_tracker.ui.viewmodel.GameTrackerViewModel
import com.hayse.sorcery.feature.home.ui.screen.HomeScreen
import com.hayse.sorcery.feature.settings.ui.screen.SettingsScreen
import com.hayse.sorcery.feature.social.ui.screen.GlobalRoomScreen
import com.hayse.sorcery.feature.social.ui.screen.RoomScreen
import com.hayse.sorcery.feature.social.ui.screen.SocialHubScreen
import com.hayse.sorcery.feature.statistics.ui.screen.StatisticsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 8 } },
    ) {
        composable(Destination.Home.route) {
            HomeScreen(
                onOpenCards = { navController.navigate(Destination.CardBrowser.route) },
                onOpenCollection = { navController.navigate(Destination.Collection.route) },
                onOpenGameMenu = { navController.navigate(Destination.GameMenu.route) },
                onOpenDecks = { navController.navigate(Destination.DeckBuilder.route) },
                onOpenSocial = { navController.navigate(Destination.Social.route) },
                onResumeGame = { navController.navigate(Destination.GameTracker.route) },
                onOpenHistory = { navController.navigate(Destination.GameHistory.route) },
            )
        }

        // Flux de partie : menu → setup → partie partagent une même instance de
        // GameTrackerViewModel (scopée au sous-graphe) pour éviter toute course de
        // persistance entre le lancement (setup) et l'affichage (tracker).
        navigation(
            startDestination = Destination.GameMenu.route,
            route = Destination.GAME_GRAPH_ROUTE,
        ) {
            composable(Destination.GameMenu.route) {
                GameMenuScreen(
                    onNewGame = { navController.navigate(Destination.GameSetup.route) },
                    onHistory = { navController.navigate(Destination.GameHistory.route) },
                )
            }
            composable(Destination.GameSetup.route) { entry ->
                val graphOwner = remember(entry) { navController.getBackStackEntry(Destination.GAME_GRAPH_ROUTE) }
                val viewModel = koinViewModel<GameTrackerViewModel>(viewModelStoreOwner = graphOwner)
                GameSetupScreen(
                    viewModel = viewModel,
                    onStarted = {
                        navController.navigate(Destination.GameTracker.route) {
                            popUpTo(Destination.GameMenu.route)
                        }
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(Destination.GameTracker.route) { entry ->
                val graphOwner = remember(entry) { navController.getBackStackEntry(Destination.GAME_GRAPH_ROUTE) }
                val viewModel = koinViewModel<GameTrackerViewModel>(viewModelStoreOwner = graphOwner)
                GameTrackerScreen(
                    viewModel = viewModel,
                    onNewGame = { navController.navigate(Destination.GameSetup.route) },
                )
            }
        }

        composable(Destination.GameHistory.route) {
            GameHistoryScreen()
        }
        composable(Destination.CardBrowser.route) {
            if (LocalWindowWidthSizeClass.current.isExpanded) {
                CardsMasterDetail()
            } else {
                CardListScreen(
                    onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
                )
            }
        }
        composable(
            route = Destination.CardDetail.route,
            arguments = listOf(navArgument(Destination.CardDetail.ARG_NAME) { type = NavType.StringType }),
        ) { entry ->
            val name = entry.arguments?.getString(Destination.CardDetail.ARG_NAME).orEmpty()
            CardDetailPager(name = name)
        }
        composable(Destination.Collection.route) {
            CollectionScreen(
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
        composable(Destination.Statistics.route) {
            StatisticsScreen()
        }
        composable(Destination.DeckBuilder.route) {
            DeckListScreen(
                onOpenDeck = { deckId -> navController.navigate(Destination.DeckEditor.routeFor(deckId)) },
            )
        }
        composable(Destination.Social.route) {
            SocialHubScreen(
                onStartGlobal = { navController.navigate(Destination.Global.route) },
                onStartRoom = { navController.navigate(Destination.Room.route) },
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
        composable(Destination.Room.route) {
            RoomScreen(
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
        composable(Destination.Global.route) {
            GlobalRoomScreen(
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
        composable(Destination.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Destination.DeckEditor.route,
            arguments = listOf(navArgument(Destination.DeckEditor.ARG_DECK_ID) { type = NavType.LongType }),
        ) { entry ->
            val deckId = entry.arguments?.getLong(Destination.DeckEditor.ARG_DECK_ID) ?: 0L
            DeckEditorScreen(
                deckId = deckId,
                onCardClick = { name -> navController.navigate(Destination.CardDetail.routeFor(name)) },
            )
        }
    }
}

/** Vue tablette : liste des cartes à gauche, détail de la carte sélectionnée à droite. */
@Composable
private fun CardsMasterDetail() {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    Row(modifier = Modifier.fillMaxSize()) {
        CardListScreen(
            onCardClick = { selected = it },
            modifier = Modifier.weight(1f),
        )
        VerticalDivider()
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            selected?.let { name -> CardDetailScreen(name = name) }
                ?: EmptyState(title = stringResource(R.string.nav_select_card))
        }
    }
}
