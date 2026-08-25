package com.hayse.sorcery.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hayse.sorcery.feature.game_tracker.ui.screen.GameTrackerScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.GameTracker.route,
        modifier = modifier,
    ) {
        composable(Destination.GameTracker.route) {
            GameTrackerScreen()
        }
    }
}
