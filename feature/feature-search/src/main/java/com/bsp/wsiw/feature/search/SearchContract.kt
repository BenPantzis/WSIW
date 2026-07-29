package com.bsp.wsiw.feature.search

import com.bsp.wsiw.core.domain.model.Movie

sealed interface SearchAction {
    data class UpdateQuery(val query: String) : SearchAction
    data object ClearQuery : SearchAction
}

sealed interface SearchEvent {
    data class ShowError(val message: String) : SearchEvent
}

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val trendingMovies: List<Movie> = emptyList(),
    val isTrendingLoading: Boolean = true,
)
