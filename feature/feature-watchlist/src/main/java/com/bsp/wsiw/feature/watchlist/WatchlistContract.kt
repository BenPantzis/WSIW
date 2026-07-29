package com.bsp.wsiw.feature.watchlist

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.ui.UiText

sealed interface WatchlistAction {
    data class RemoveMovie(val movieId: Int) : WatchlistAction
}

sealed interface WatchlistEvent {
    data class ShowSnackbar(val message: UiText) : WatchlistEvent
}

data class WatchlistUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
)
