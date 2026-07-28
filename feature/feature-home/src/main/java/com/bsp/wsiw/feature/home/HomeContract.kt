package com.bsp.wsiw.feature.home

import com.bsp.wsiw.core.domain.model.Movie

sealed interface HomeAction {
    data object LoadMovies : HomeAction
    data object Retry : HomeAction
    data object Refresh : HomeAction
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
)
