package com.bsp.wsiw.feature.home

import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie

enum class HomeCategory(val label: String, val apiKey: String?) {
    Popular("Popular", null),
    Trending("Trending", "trending"),
    TopRated("Top Rated", "top_rated"),
    NowPlaying("Now Playing", "now_playing"),
    Upcoming("Upcoming", "upcoming"),
    ByGenre("By Genre", null),
}

sealed interface HomeAction {
    data object LoadMovies : HomeAction
    data object Retry : HomeAction
    data object Refresh : HomeAction
    data class SelectCategory(val category: HomeCategory) : HomeAction
    data class SelectGenre(val genreId: Int) : HomeAction
}

sealed interface HomeEvent {
    data class ShowSnackbar(val message: String) : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val selectedCategory: HomeCategory = HomeCategory.Popular,
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val isGenresLoading: Boolean = false,
)
