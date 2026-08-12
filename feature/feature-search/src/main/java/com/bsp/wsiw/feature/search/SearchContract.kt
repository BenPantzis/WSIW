package com.bsp.wsiw.feature.search

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.SearchResult
import com.bsp.wsiw.core.ui.UiText

sealed interface SearchAction {
    data class UpdateQuery(val query: String) : SearchAction
    data object ClearQuery : SearchAction
}

sealed interface SearchEvent {
    data class ShowError(val message: UiText) : SearchEvent
}

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: UiText? = null,
    val trendingMovies: List<Movie> = emptyList(),
    val isTrendingLoading: Boolean = true,
)
