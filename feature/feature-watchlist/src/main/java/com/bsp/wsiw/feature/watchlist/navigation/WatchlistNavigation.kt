@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.watchlist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.watchlist.WatchlistScreen
import kotlinx.serialization.Serializable

@Serializable
data object WatchlistKey : NavKey

fun EntryProviderScope<NavKey>.watchlistDestination(
    onMovieClick: (Int) -> Unit,
    onBrowseMovies: () -> Unit,
) {
    entry<WatchlistKey> {
        WatchlistScreen(
            onMovieClick = onMovieClick,
            onBrowseMovies = onBrowseMovies,
        )
    }
}
