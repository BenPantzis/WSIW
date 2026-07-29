@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.detail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.detail.DetailScreen
import com.bsp.wsiw.feature.detail.PersonScreen
import com.bsp.wsiw.feature.detail.ReviewsScreen
import kotlinx.serialization.Serializable

@Serializable
data class DetailKey(val movieId: Int) : NavKey

@Serializable
data class PersonKey(val personId: Int) : NavKey

@Serializable
data class ReviewsKey(val movieId: Int, val movieTitle: String) : NavKey

fun EntryProviderScope<NavKey>.detailDestination(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
) {
    entry<DetailKey> { key ->
        DetailScreen(
            movieId = key.movieId,
            onBack = onBack,
            onMovieClick = onMovieClick,
            onPersonClick = onPersonClick,
            onReviewsClick = onReviewsClick,
        )
    }
}

fun EntryProviderScope<NavKey>.reviewsDestination(onBack: () -> Unit) {
    entry<ReviewsKey> { key ->
        ReviewsScreen(
            movieId = key.movieId,
            movieTitle = key.movieTitle,
            onBack = onBack,
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
