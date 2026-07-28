package com.bsp.wsiw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.bsp.wsiw.core.ui.theme.WSIWTheme
import com.bsp.wsiw.feature.detail.navigation.DetailKey
import com.bsp.wsiw.feature.detail.navigation.detailDestination
import com.bsp.wsiw.feature.home.navigation.HomeKey
import com.bsp.wsiw.feature.home.navigation.homeDestination
import com.bsp.wsiw.feature.search.navigation.SearchKey
import com.bsp.wsiw.feature.search.navigation.searchDestination
import com.bsp.wsiw.feature.watchlist.navigation.WatchlistKey
import com.bsp.wsiw.feature.watchlist.navigation.watchlistDestination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WSIWTheme {
                WsiwApp()
            }
        }
    }
}

@Composable
private fun WsiwApp() {
    val backStack = remember { mutableStateListOf<NavKey>(HomeKey) }

    val currentKey by remember { derivedStateOf { backStack.lastOrNull() } }
    val showBottomBar by remember {
        derivedStateOf {
            currentKey.let { it is HomeKey || it is SearchKey || it is WatchlistKey }
        }
    }

    fun navigateToTab(key: NavKey) {
        if (currentKey?.let { it::class == key::class } == true) return
        backStack.clear()
        backStack.add(key)
    }

    fun navigateToDetail(movieId: Int) {
        backStack.add(DetailKey(movieId))
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                WsiwNavigationBar(
                    currentKey = currentKey,
                    onTabSelected = ::navigateToTab,
                )
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                homeDestination(onMovieClick = ::navigateToDetail)
                searchDestination(onMovieClick = ::navigateToDetail)
                watchlistDestination(
                    onMovieClick = ::navigateToDetail,
                    onBrowseMovies = { navigateToTab(HomeKey) },
                )
                detailDestination(onBack = { backStack.removeLastOrNull() })
            },
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(innerPadding),
        )
    }
}

private enum class Tab(
    val key: NavKey,
    val label: String,
    val icon: ImageVector,
) {
    Discover(HomeKey, "Discover", Icons.Default.Home),
    Search(SearchKey, "Search", Icons.Default.Search),
    Watchlist(WatchlistKey, "Watchlist", Icons.Default.Favorite),
}

@Composable
private fun WsiwNavigationBar(
    currentKey: NavKey?,
    onTabSelected: (NavKey) -> Unit,
) {
    NavigationBar {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentKey?.let { it::class == tab.key::class } == true,
                onClick = { onTabSelected(tab.key) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}
