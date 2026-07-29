package com.bsp.wsiw.feature.watchlist

import androidx.annotation.StringRes
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.ui.UiText

enum class WatchlistSort(@param:StringRes val labelRes: Int) {
    DateAdded(R.string.watchlist_sort_date_added),
    TitleAZ(R.string.watchlist_sort_title_az),
    Rating(R.string.watchlist_sort_rating),
}

enum class WatchlistViewMode { Grid, List }

sealed interface WatchlistAction {
    data class RemoveMovie(val movieId: Int) : WatchlistAction
    data class UndoRemove(val movie: Movie) : WatchlistAction
    data class SelectSort(val sort: WatchlistSort) : WatchlistAction
    data object ToggleViewMode : WatchlistAction
}

sealed interface WatchlistEvent {
    data class ShowSnackbar(val message: UiText, val undoMovie: Movie? = null) : WatchlistEvent
}

data class WatchlistUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val sort: WatchlistSort = WatchlistSort.DateAdded,
    val viewMode: WatchlistViewMode = WatchlistViewMode.Grid,
) {
    val sortedMovies: List<Movie> get() = when (sort) {
        WatchlistSort.DateAdded -> movies
        WatchlistSort.TitleAZ -> movies.sortedBy { it.title }
        WatchlistSort.Rating -> movies.sortedByDescending { it.voteAverage }
    }
}
