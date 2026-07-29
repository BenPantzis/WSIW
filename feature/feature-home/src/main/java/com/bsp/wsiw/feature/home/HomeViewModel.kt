package com.bsp.wsiw.feature.home

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.domain.usecase.GetPopularMoviesUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMovies: GetPopularMoviesUseCase,
    private val movieRepository: MovieRepository,
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
            is HomeAction.SelectCategory -> {
                if (uiState.value.selectedCategory == action.category) return
                updateState {
                    copy(
                        selectedCategory = action.category,
                        movies = emptyList(),
                        error = null,
                        isLoading = true,
                    )
                }
                if (action.category == HomeCategory.ByGenre) {
                    loadGenresIfNeeded()
                } else {
                    loadMovies()
                }
            }
            is HomeAction.SelectGenre -> {
                updateState { copy(selectedGenreId = action.genreId, movies = emptyList(), isLoading = true) }
                loadMovies()
            }
        }
    }

    private fun moviesSource(forceRefresh: Boolean): Flow<Result<List<Movie>>> {
        val state = uiState.value
        return when {
            state.selectedCategory == HomeCategory.ByGenre && state.selectedGenreId != null ->
                movieRepository.discoverMovies(state.selectedGenreId)
            state.selectedCategory == HomeCategory.ByGenre ->
                flow { }  // waiting for genre selection
            state.selectedCategory == HomeCategory.Popular ->
                getPopularMovies(GetPopularMoviesUseCase.Params(page = 1, forceRefresh = forceRefresh))
            else ->
                movieRepository.getMoviesByCategory(state.selectedCategory.apiKey!!)
        }
    }

    private fun loadMovies(forceRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            moviesSource(forceRefresh).collect { result ->
                when (result) {
                    Result.Loading -> updateState {
                        copy(isLoading = movies.isEmpty(), error = null, isRefreshing = false)
                    }
                    is Result.Success -> {
                        val movies = result.data
                        if (movies.isEmpty() && result.isRefreshing) {
                            updateState { copy(isLoading = true, isRefreshing = false) }
                        } else {
                            updateState {
                                copy(
                                    isLoading = false,
                                    movies = movies,
                                    error = null,
                                    isRefreshing = result.isRefreshing,
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

    private fun loadGenresIfNeeded() {
        val state = uiState.value
        if (state.genres.isNotEmpty()) {
            val firstId = state.selectedGenreId ?: state.genres.firstOrNull()?.id
            if (firstId != null) {
                updateState { copy(selectedGenreId = firstId, isLoading = true) }
                loadMovies()
            }
            return
        }
        viewModelScope.launch {
            updateState { copy(isGenresLoading = true) }
            movieRepository.getGenres().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val firstId = result.data.firstOrNull()?.id
                        updateState {
                            copy(
                                genres = result.data,
                                isGenresLoading = false,
                                selectedGenreId = firstId,
                            )
                        }
                        if (firstId != null) loadMovies()
                    }
                    is Result.Error -> updateState { copy(isGenresLoading = false, isLoading = false) }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
