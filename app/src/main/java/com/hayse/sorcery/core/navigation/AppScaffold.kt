package com.hayse.sorcery.core.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.CardListContext
import com.hayse.sorcery.core.ui.LocalCardListContext
import com.hayse.sorcery.core.ui.LocalTopBarActions
import com.hayse.sorcery.core.ui.LocalTopBarSearch
import com.hayse.sorcery.core.ui.LocalWindowWidthSizeClass
import com.hayse.sorcery.core.ui.TopBarActionsSlot
import com.hayse.sorcery.core.ui.TopBarActionsState
import com.hayse.sorcery.core.ui.TopBarSearchState
import com.hayse.sorcery.core.ui.isAtLeastMedium
import com.hayse.sorcery.core.ui.theme.skin.SkinnedBackground
import com.hayse.sorcery.core.ui.theme.skin.SkinnedTopBar
import kotlinx.coroutines.launch

/**
 * Coquille de l'app. Sur téléphone (Compact) : drawer latéral modal + barre supérieure.
 * Sur tablette (Medium/Expanded) : rail de navigation permanent à gauche.
 */
@Composable
fun AppScaffold(widthSizeClass: WindowWidthSizeClass, socialEnabled: Boolean = true) {
    val navController = rememberNavController()

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route ?: Destination.Home.route
    val title = stringResource(Destination.labelForRoute(currentRoute))

    val topBarSearch = remember { TopBarSearchState() }
    val topBarActions = remember { TopBarActionsState() }
    val cardListContext = remember { CardListContext() }

    val destinations = Destination.drawerDestinations
        .filter { it != Destination.Social || socialEnabled }

    CompositionLocalProvider(
        LocalWindowWidthSizeClass provides widthSizeClass,
        LocalTopBarSearch provides topBarSearch,
        LocalTopBarActions provides topBarActions,
        LocalCardListContext provides cardListContext,
    ) {
        if (widthSizeClass.isAtLeastMedium) {
            RailScaffold(navController, currentRoute, title, destinations)
        } else {
            DrawerScaffold(navController, currentRoute, title, destinations)
        }
    }
}

/** Titre de la barre : champ de recherche si un écran le fournit et qu'il est déployé, sinon le titre. */
@Composable
private fun TopBarContent(title: String) {
    val search = LocalTopBarSearch.current
    if (!search.visible || !search.expanded) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        return
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    TextField(
        value = search.query,
        onValueChange = {
            search.query = it
            search.onQueryChange(it)
        },
        placeholder = { Text(search.placeholder) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
    )
}

/** Icône loupe repliée ; une fois déployée, elle devient une croix qui referme et vide la recherche. */
@Composable
private fun TopBarSearchAction() {
    val search = LocalTopBarSearch.current
    if (!search.visible) return
    if (search.expanded) {
        IconButton(onClick = {
            search.expanded = false
            search.query = ""
            search.onQueryChange("")
        }) {
            Icon(Icons.Filled.Close, contentDescription = "Fermer la recherche")
        }
    } else {
        IconButton(onClick = { search.expanded = true }) {
            Icon(Icons.Filled.Search, contentDescription = "Rechercher")
        }
    }
}

/** Navigue vers une destination du menu en préservant l'état inter-onglets. */
private fun navigateToTopLevel(navController: NavHostController, destination: Destination, currentRoute: String) {
    if (destination.route == currentRoute) return
    navController.navigate(destination.route) {
        launchSingleTop = true
        if (destination == Destination.Home) {
            // Accueil = destination de départ : on vide la pile jusqu'à l'accueil inclus pour
            // repartir sur un écran frais. Le combo saveState/restoreState devient un no-op quand
            // la cible est déjà la start destination → le retour accueil échouait parfois.
            popUpTo(Destination.Home.route) { inclusive = true }
        } else {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            restoreState = true
        }
    }
}

/** Une entrée est sélectionnée si sa route est active, ou (pour Suivi de partie) si on est dans le sous-graphe partie. */
private fun isSelected(destination: Destination, currentRoute: String): Boolean = when {
    destination.route == currentRoute -> true
    destination == Destination.GameMenu -> currentRoute in gameGraphRoutes
    else -> false
}

private val gameGraphRoutes = setOf(
    Destination.GameMenu.route,
    Destination.GameSetup.route,
    Destination.GameTracker.route,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerScaffold(
    navController: NavHostController,
    currentRoute: String,
    title: String,
    destinations: List<Destination>,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                destinations.forEach { destination ->
                    NavigationDrawerItem(
                        icon = { Icon(drawerIcon(destination), contentDescription = null) },
                        label = { Text(stringResource(destination.labelRes)) },
                        selected = destination.route == currentRoute,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateToTopLevel(navController, destination, currentRoute)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SkinnedTopBar(
                    title = { TopBarContent(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.nav_menu))
                        }
                    },
                    actions = {
                        TopBarActionsSlot()
                        TopBarSearchAction()
                    },
                )
            },
        ) { innerPadding ->
            SkinnedBackground {
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RailScaffold(
    navController: NavHostController,
    currentRoute: String,
    title: String,
    destinations: List<Destination>,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            destinations.forEach { destination ->
                NavigationRailItem(
                    icon = { Icon(drawerIcon(destination), contentDescription = null) },
                    label = { Text(stringResource(destination.labelRes)) },
                    selected = isSelected(destination, currentRoute),
                    onClick = { navigateToTopLevel(navController, destination, currentRoute) },
                )
            }
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SkinnedTopBar(
                    title = { TopBarContent(title) },
                    actions = {
                        TopBarActionsSlot()
                        TopBarSearchAction()
                    },
                )
            },
        ) { innerPadding ->
            SkinnedBackground {
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        )
        Text("Sorcery", style = MaterialTheme.typography.titleLarge)
    }
}

private fun drawerIcon(destination: Destination): ImageVector = when (destination) {
    Destination.Home -> Icons.Filled.Home
    Destination.CardBrowser -> Icons.Filled.Style
    Destination.Collection -> Icons.Filled.CollectionsBookmark
    Destination.Statistics -> Icons.Filled.BarChart
    Destination.GameMenu -> Icons.Filled.SportsEsports
    Destination.DeckBuilder -> Icons.Filled.Dashboard
    Destination.Social -> Icons.Filled.SwapHoriz
    Destination.Settings -> Icons.Filled.Settings
    else -> Icons.Filled.Home
}
