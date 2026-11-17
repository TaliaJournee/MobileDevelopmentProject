package edu.ap.dakaniktochniwegsmijten.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.ap.dakaniktochniwegsmijten.R
import edu.ap.dakaniktochniwegsmijten.ui.auth.LoginScreen
import edu.ap.dakaniktochniwegsmijten.ui.auth.SessionViewModel
import edu.ap.dakaniktochniwegsmijten.ui.map.MapScreen

private const val SESSION_ROUTE = "session"
private const val LOGIN_ROUTE = "login"

private enum class CustomerDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Discover("discover", R.string.nav_discover, Icons.Default.Search),
    Map("map", R.string.nav_map, Icons.Default.LocationOn),
    Orders("orders", R.string.nav_orders, Icons.Default.ShoppingCart),
    Favorites("favorites", R.string.nav_favorites, Icons.Default.Favorite)
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = viewModel()
) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var lastUserId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(sessionState.isLoading, sessionState.userId) {
        if (!sessionState.isLoading) {
            val isInitialNavigation =
                navController.currentDestination?.route == SESSION_ROUTE
            val hasUserChanged = lastUserId != sessionState.userId

            if (isInitialNavigation || hasUserChanged) {
                CustomerDestination.entries.forEach { destination ->
                    navController.clearBackStack(destination.route)
                }

                val targetRoute = if (sessionState.userId == null) {
                    LOGIN_ROUTE
                } else {
                    CustomerDestination.Discover.route
                }

                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.id) {
                        inclusive = false
                        saveState = false
                    }
                    launchSingleTop = true
                }

                lastUserId = sessionState.userId
            }
        }
    }

    val showCustomerNavigation =
        !sessionState.isLoading &&
                sessionState.userId != null &&
                CustomerDestination.entries.any { it.route == currentRoute }

    Column(modifier = modifier.fillMaxSize()) {
        if (showCustomerNavigation) {
            TextButton(
                onClick = sessionViewModel::signOut,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 8.dp)
            ) {
                Text(stringResource(R.string.sign_out))
            }
        }

        NavHost(
            navController = navController,
            startDestination = SESSION_ROUTE,
            modifier = Modifier.weight(1f)
        ) {
            composable(SESSION_ROUTE) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.session_loading))
                }
            }

            composable(LOGIN_ROUTE) {
                LoginScreen()
            }

            composable(CustomerDestination.Discover.route) {
                PlaceholderScreen(R.string.nav_discover)
            }

            composable(CustomerDestination.Map.route) {
                MapScreen()
            }

            composable(CustomerDestination.Orders.route) {
                PlaceholderScreen(R.string.nav_orders)
            }

            composable(CustomerDestination.Favorites.route) {
                PlaceholderScreen(R.string.nav_favorites)
            }
        }

        if (showCustomerNavigation) {
            NavigationBar(
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                CustomerDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(CustomerDestination.Discover.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(stringResource(destination.labelRes))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(@StringRes titleRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}