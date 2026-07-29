@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.detail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.detail.DetailScreen
import com.bsp.wsiw.feature.detail.PersonScreen
import kotlinx.serialization.Serializable

@Serializable
data class DetailKey(val movieId: Int) : NavKey

@Serializable
data class PersonKey(val personId: Int) : NavKey

fun EntryProviderScope<NavKey>.detailDestination(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
) {
    entry<DetailKey> { key ->
        DetailScreen(
            movieId = key.movieId,
            onBack = onBack,
            onMovieClick = onMovieClick,
            onPersonClick = onPersonClick,
        )
    }
}

fun EntryProviderScope<NavKey>.personDestination(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    entry<PersonKey> { key ->
        PersonScreen(
            personId = key.personId,
            onBack = onBack,
            onMovieClick = onMovieClick,
        )
    }
}
