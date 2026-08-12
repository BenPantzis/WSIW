package com.bsp.wsiw.feature.watchlist

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.repository.WatchlistPreferences
import com.bsp.wsiw.core.domain.usecase.GetAllRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.RefreshRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.RefreshWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlist: GetWatchlistUseCase,
    private val toggleWatchlist: ToggleWatchlistUseCase,
    private val refreshWatchlist: RefreshWatchlistUseCase,
    private val getAllRatings: GetAllRatingsUseCase,
    private val refreshRatings: RefreshRatingsUseCase,
    private val preferences: WatchlistPreferences,
) : BaseViewModel<WatchlistAction, WatchlistEvent, WatchlistUiState>(
    initialState = WatchlistUiState(),
) {
    init {
        viewModelScope.launch {
            val isList = preferences.isListView.first()
            updateState { copy(viewMode = if (isList) WatchlistViewMode.List else WatchlistViewMode.Grid) }
        }
        viewModelScope.launch {
            getWatchlist().collect { movies ->
                updateState { copy(isLoading = false, movies = movies) }
            }
        }
        viewModelScope.launch {
            getAllRatings().collect { ratingsMap ->
                updateState { copy(ratings = ratingsMap) }
            }
        }
        viewModelScope.launch { refreshWatchlist() }
        viewModelScope.launch { refreshRatings() }
    }

    override fun handleAction(action: WatchlistAction) {
        when (action) {
            is WatchlistAction.RemoveMovie -> {
                val movie = uiState.value.movies.find { it.id == action.movieId } ?: return
                viewModelScope.launch {
                    toggleWatchlist(movie, isWatchlisted = true)
                    sendEvent(WatchlistEvent.ShowSnackbar(
                        message = UiText.StringResource(R.string.watchlist_snackbar_removed),
                        undoMovie = movie,
                    ))
                }
            }
            is WatchlistAction.UndoRemove -> {
                updateState {
                    copy(undoVersions = undoVersions + (action.movie.id to (undoVersions[action.movie.id] ?: 0) + 1))
                }
                viewModelScope.launch { toggleWatchlist(action.movie, isWatchlisted = false) }
            }
            is WatchlistAction.SelectSort -> updateState { copy(sort = action.sort) }
            WatchlistAction.ToggleViewMode -> {
                val newMode = if (uiState.value.viewMode == WatchlistViewMode.Grid) WatchlistViewMode.List else WatchlistViewMode.Grid
                updateState { copy(viewMode = newMode) }
                viewModelScope.launch { preferences.setListView(newMode == WatchlistViewMode.List) }
            }
        }
    }
}
