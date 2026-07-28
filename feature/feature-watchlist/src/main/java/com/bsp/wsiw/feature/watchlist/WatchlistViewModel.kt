package com.bsp.wsiw.feature.watchlist

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlist: GetWatchlistUseCase,
    private val toggleWatchlist: ToggleWatchlistUseCase,
) : BaseViewModel<WatchlistAction, WatchlistEvent, WatchlistUiState>(
    initialState = WatchlistUiState(),
) {
    init {
        viewModelScope.launch {
            getWatchlist().collect { movies ->
                updateState { copy(isLoading = false, movies = movies) }
            }
        }
    }

    override fun handleAction(action: WatchlistAction) {
        when (action) {
            is WatchlistAction.RemoveMovie -> {
                val movie = uiState.value.movies.find { it.id == action.movieId } ?: return
                viewModelScope.launch {
                    toggleWatchlist(movie, isWatchlisted = true)
                    sendEvent(WatchlistEvent.ShowSnackbar("Removed from watchlist"))
                }
            }
        }
    }
}
