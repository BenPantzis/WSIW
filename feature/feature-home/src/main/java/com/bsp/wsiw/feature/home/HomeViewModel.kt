package com.bsp.wsiw.feature.home

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.usecase.GetPopularMoviesUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMovies: GetPopularMoviesUseCase,
) : BaseViewModel<HomeAction, HomeEvent, HomeUiState>(
    initialState = HomeUiState(),
) {
    private var loadJob: Job? = null

    init {
        onAction(HomeAction.LoadMovies)
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            HomeAction.LoadMovies -> loadMovies()
            HomeAction.Retry -> loadMovies()
            HomeAction.Refresh -> {
                updateState { copy(isPullRefreshing = true) }
                loadMovies(forceRefresh = true)
            }
        }
    }

    private fun loadMovies(forceRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getPopularMovies(GetPopularMoviesUseCase.Params(page = 1, forceRefresh = forceRefresh)).collect { result ->
                when (result) {
                    Result.Loading -> updateState {
                        copy(isLoading = movies.isEmpty(), error = null, isRefreshing = false)
                    }
                    is Result.Success -> {
                        val movies = result.data
                        if (movies.isEmpty() && result.isRefreshing) {
                            // No cache yet — stay in shimmer while the network request runs
                            updateState { copy(isLoading = true, isRefreshing = false) }
                        } else {
                            updateState {
                                copy(
                                    isLoading = false,
                                    movies = movies,
                                    error = null,
                                    isRefreshing = result.isRefreshing,
                                    // Pull spinner stays up while refreshing; clears when done
                                    isPullRefreshing = result.isRefreshing && isPullRefreshing,
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        if (uiState.value.movies.isNotEmpty()) {
                            updateState { copy(isRefreshing = false, isPullRefreshing = false) }
                            sendEvent(HomeEvent.ShowSnackbar("Couldn't refresh — showing cached data"))
                        } else {
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    isPullRefreshing = false,
                                    error = result.exception?.message ?: "Something went wrong",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
