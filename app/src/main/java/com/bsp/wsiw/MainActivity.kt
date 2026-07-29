package com.bsp.wsiw

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.bsp.wsiw.core.ui.theme.WSIWTheme
import com.bsp.wsiw.feature.detail.navigation.DetailKey
import com.bsp.wsiw.feature.detail.navigation.PersonKey
import com.bsp.wsiw.feature.detail.navigation.detailDestination
import com.bsp.wsiw.feature.detail.navigation.personDestination
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
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setOnExitAnimationListener { provider ->
            val decor = window.decorView as ViewGroup
            if (decor.height > 0) {
                runLetterboxAnimation(provider, decor)
            } else {
                decor.post { runLetterboxAnimation(provider, decor) }
            }
        }

        setContent {
            WSIWTheme {
                WsiwApp()
            }
        }
    }

    private fun runLetterboxAnimation(provider: SplashScreenViewProvider, decor: ViewGroup) {
        val screenH = decor.height
        val barH = (screenH * 0.38f).toInt()
        val easing = AccelerateDecelerateInterpolator()

        val topBar = View(this).apply {
            setBackgroundColor(CINEMA_BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, barH, Gravity.TOP
            )
            translationY = -barH.toFloat()
        }
        val bottomBar = View(this).apply {
            setBackgroundColor(CINEMA_BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, barH, Gravity.BOTTOM
            )
            translationY = barH.toFloat()
        }
        decor.addView(topBar)
        decor.addView(bottomBar)

        // iconView throws NPE when the system splash has no icon layer — handle gracefully
        val iconView = runCatching { provider.iconView }.getOrNull()

        val animators = buildList {
            add(ObjectAnimator.ofFloat(topBar, View.TRANSLATION_Y, 0f)
                .also { it.duration = SLIDE_MS; it.interpolator = easing })
            add(ObjectAnimator.ofFloat(bottomBar, View.TRANSLATION_Y, 0f)
                .also { it.duration = SLIDE_MS; it.interpolator = easing })
            if (iconView != null) {
                add(ObjectAnimator.ofFloat(iconView, View.ALPHA, 0f)
                    .also { it.duration = FADE_MS; it.interpolator = easing })
            }
        }
        val slideIn = AnimatorSet().apply { playTogether(animators) }
        slideIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                provider.remove()
                topBar.postDelayed({
                    val slideOut = AnimatorSet().apply {
                        playTogether(
                            ObjectAnimator.ofFloat(topBar, View.TRANSLATION_Y, -barH.toFloat())
                                .also { it.duration = SLIDE_MS; it.interpolator = easing },
                            ObjectAnimator.ofFloat(bottomBar, View.TRANSLATION_Y, barH.toFloat())
                                .also { it.duration = SLIDE_MS; it.interpolator = easing },
                        )
                    }
                    slideOut.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            decor.removeView(topBar)
                            decor.removeView(bottomBar)
                        }
                    })
                    slideOut.start()
                }, HOLD_MS)
            }
        })
        slideIn.start()
    }

    companion object {
        private const val SLIDE_MS = 450L
        private const val FADE_MS  = 350L
        private const val HOLD_MS  = 300L
        private val CINEMA_BLACK   = 0xFF0D0D0D.toInt()
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

    fun navigateToPerson(personId: Int) {
        backStack.add(PersonKey(personId))
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
            transitionSpec = {
                if (targetState.entries.lastOrNull()?.contentKey.toString().startsWith("DetailKey")) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith ExitTransition.None
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            popTransitionSpec = {
                if (initialState.entries.lastOrNull()?.contentKey.toString().startsWith("DetailKey")) {
                    EnterTransition.None togetherWith (slideOutHorizontally { it } + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            predictivePopTransitionSpec = {
                if (initialState.entries.lastOrNull()?.contentKey.toString().startsWith("DetailKey")) {
                    EnterTransition.None togetherWith (slideOutHorizontally { it } + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
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
                detailDestination(
                    onBack = { backStack.removeLastOrNull() },
                    onMovieClick = ::navigateToDetail,
                    onPersonClick = ::navigateToPerson,
                )
                personDestination(
                    onBack = { backStack.removeLastOrNull() },
                    onMovieClick = ::navigateToDetail,
                )
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
