package com.bsp.wsiw

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.bsp.wsiw.feature.detail.navigation.ReviewsKey
import com.bsp.wsiw.feature.detail.navigation.detailDestination
import com.bsp.wsiw.feature.detail.navigation.personDestination
import com.bsp.wsiw.feature.detail.navigation.reviewsDestination
import com.bsp.wsiw.feature.home.navigation.HomeKey
import com.bsp.wsiw.feature.home.navigation.homeDestination
import com.bsp.wsiw.feature.profile.navigation.LoginCallbackKey
import com.bsp.wsiw.feature.profile.navigation.ProfileKey
import com.bsp.wsiw.feature.profile.navigation.RatedMoviesKey
import com.bsp.wsiw.feature.profile.navigation.loginCallbackDestination
import com.bsp.wsiw.feature.profile.navigation.profileDestination
import com.bsp.wsiw.feature.profile.navigation.ratedMoviesDestination
import com.bsp.wsiw.feature.search.navigation.SearchKey
import com.bsp.wsiw.feature.search.navigation.searchDestination
import com.bsp.wsiw.feature.tv.navigation.TvDetailKey
import com.bsp.wsiw.feature.tv.navigation.TvKey
import com.bsp.wsiw.feature.tv.navigation.tvDetailDestination
import com.bsp.wsiw.feature.tv.navigation.tvListDestination
import com.bsp.wsiw.feature.watchlist.navigation.WatchlistKey
import com.bsp.wsiw.feature.watchlist.navigation.watchlistDestination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingAuthToken = mutableStateOf<String?>(null)

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

        intent?.data?.extractAuthToken()?.let { pendingAuthToken.value = it }

        setContent {
            WSIWTheme {
                WsiwApp(
                    pendingAuthToken = pendingAuthToken.value,
                    onAuthTokenConsumed = { pendingAuthToken.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.extractAuthToken()?.let { pendingAuthToken.value = it }
    }

    private fun Uri.extractAuthToken(): String? =
        if (scheme == "wsiw" && host == "auth") getQueryParameter("request_token") else null

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
private fun WsiwApp(
    pendingAuthToken: String?,
    onAuthTokenConsumed: () -> Unit,
) {
    val backStack = remember { mutableStateListOf<NavKey>(HomeKey) }

    val currentKey by remember { derivedStateOf { backStack.lastOrNull() } }
    val showBottomBar by remember {
        derivedStateOf {
            currentKey.let {
                it is HomeKey || it is SearchKey || it is WatchlistKey || it is TvKey || it is ProfileKey
            }
        }
    }

    LaunchedEffect(pendingAuthToken) {
        val token = pendingAuthToken ?: return@LaunchedEffect
        backStack.add(LoginCallbackKey(token))
        onAuthTokenConsumed()
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

    fun navigateToReviews(movieId: Int, movieTitle: String) {
        backStack.add(ReviewsKey(movieId, movieTitle))
    }

    fun navigateToTvDetail(seriesId: Int) {
        backStack.add(TvDetailKey(seriesId))
    }

    fun navigateToRatedMovies() {
        backStack.add(RatedMoviesKey)
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
                if (targetState.entries.lastOrNull()?.contentKey.let {
                        it is DetailKey || it is TvDetailKey || it is RatedMoviesKey
                    }) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith ExitTransition.None
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            popTransitionSpec = {
                if (initialState.entries.lastOrNull()?.contentKey.let {
                        it is DetailKey || it is TvDetailKey || it is RatedMoviesKey
                    }) {
                    EnterTransition.None togetherWith (slideOutHorizontally { it } + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            predictivePopTransitionSpec = {
                if (initialState.entries.lastOrNull()?.contentKey.let {
                        it is DetailKey || it is TvDetailKey || it is RatedMoviesKey
                    }) {
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
                searchDestination(
                    onMovieClick = ::navigateToDetail,
                    onPersonClick = ::navigateToPerson,
                    onTvShowClick = ::navigateToTvDetail,
                )
                watchlistDestination(
                    onMovieClick = ::navigateToDetail,
                    onBrowseMovies = { navigateToTab(HomeKey) },
                )
                profileDestination(onSeeAllRatings = ::navigateToRatedMovies)
                ratedMoviesDestination(onBack = { backStack.removeLastOrNull() })
                loginCallbackDestination(
                    onSuccess = {
                        backStack.removeLastOrNull()
                        navigateToTab(ProfileKey)
                    },
                    onBack = { backStack.removeLastOrNull() },
                )
                detailDestination(
                    onBack = { backStack.removeLastOrNull() },
                    onMovieClick = ::navigateToDetail,
                    onPersonClick = ::navigateToPerson,
                    onReviewsClick = ::navigateToReviews,
                )
                personDestination(
                    onBack = { backStack.removeLastOrNull() },
                    onMovieClick = ::navigateToDetail,
                )
                tvListDestination(onShowClick = ::navigateToTvDetail)
                tvDetailDestination(
                    onBack = { backStack.removeLastOrNull() },
                    onShowClick = ::navigateToTvDetail,
                    onPersonClick = ::navigateToPerson,
                )
                reviewsDestination(onBack = { backStack.removeLastOrNull() })
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
    Discover(HomeKey, "Movies", Icons.Default.Home),
    Tv(TvKey, "TV", Icons.Default.PlayArrow),
    Search(SearchKey, "Search", Icons.Default.Search),
    Watchlist(WatchlistKey, "Watchlist", Icons.Default.Favorite),
    Profile(ProfileKey, "Profile", Icons.Default.Person),
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
