package com.bsp.wsiw.feature.watchlist

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.datastore.PreferencesRepository
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
    private val preferences: PreferencesRepository,
) : BaseViewModel<WatchlistAction, WatchlistEvent, WatchlistUiState>(
    initialState = WatchlistUiState(),
) {
    init {
        viewModelScope.launch {
            val isList = preferences.preferences.first()[KEY_LIST_VIEW] ?: false
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
                viewModelScope.launch { toggleWatchlist(action.movie, isWatchlisted = false) }
            }
            is WatchlistAction.SelectSort -> updateState { copy(sort = action.sort) }
            WatchlistAction.ToggleViewMode -> {
                val newMode = if (uiState.value.viewMode == WatchlistViewMode.Grid) WatchlistViewMode.List else WatchlistViewMode.Grid
                updateState { copy(viewMode = newMode) }
                viewModelScope.launch { preferences.put(KEY_LIST_VIEW, newMode == WatchlistViewMode.List) }
            }
        }
    }

    private companion object {
        val KEY_LIST_VIEW = booleanPreferencesKey("watchlist_list_view")
    }
}
