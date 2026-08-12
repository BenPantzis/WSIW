@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.search.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object SearchKey : NavKey

fun EntryProviderScope<NavKey>.searchDestination(
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
) {
    entry<SearchKey> {
        SearchScreen(
            onMovieClick = onMovieClick,
            onPersonClick = onPersonClick,
            onTvShowClick = onTvShowClick,
        )
    }
}
