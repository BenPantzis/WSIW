@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.detail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.detail.DetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class DetailKey(val movieId: Int) : NavKey

fun EntryProviderScope<NavKey>.detailDestination(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    entry<DetailKey> { key ->
        DetailScreen(movieId = key.movieId, onBack = onBack, onMovieClick = onMovieClick)
    }
}
