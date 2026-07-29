package com.bsp.wsiw.feature.home

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.R as CoreUiR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.Movie

@HiltViewModel
class HomeViewModel @Inject constructor(
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
            HomeAction.LoadMovies -> loadPage(page = 1, replace = true)
            HomeAction.Retry -> loadPage(page = 1, replace = true)
            HomeAction.Refresh -> {
                updateState { copy(isPullRefreshing = true) }
                loadPage(page = 1, replace = true)
            }
            HomeAction.LoadNextPage -> {
                val state = uiState.value
                if (state.canLoadMore) {
                    loadPage(page = state.currentPage + 1, replace = false)
                }
            }
            is HomeAction.SelectCategory -> {
                if (uiState.value.selectedCategory == action.category) return
                updateState {
                    copy(
                        selectedCategory = action.category,
                        movies = emptyList(),
                        error = null,
                        isLoading = true,
                        currentPage = 1,
                        totalPages = Int.MAX_VALUE,
                    )
                }
                if (action.category == HomeCategory.ByGenre) {
                    loadGenresIfNeeded()
                } else {
                    loadPage(page = 1, replace = true)
                }
            }
            is HomeAction.SelectGenre -> {
                updateState {
                    copy(selectedGenreId = action.genreId, movies = emptyList(), isLoading = true, currentPage = 1, totalPages = Int.MAX_VALUE)
                }
                loadPage(page = 1, replace = true)
            }
        }
    }

    private fun pageSource(page: Int): Flow<Result<PagedResult<Movie>>> {
        val state = uiState.value
        return when {
            state.selectedCategory == HomeCategory.ByGenre && state.selectedGenreId != null ->
                movieRepository.discoverMovies(state.selectedGenreId, page)
            state.selectedCategory == HomeCategory.ByGenre ->
                flow { }
            else ->
                movieRepository.getMoviesByCategory(state.selectedCategory.apiKey ?: "popular", page)
        }
    }

    private fun loadPage(page: Int, replace: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (replace) {
                updateState { copy(isLoading = movies.isEmpty(), isLoadingMore = false) }
            } else {
                updateState { copy(isLoadingMore = true) }
            }
            pageSource(page).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val paged = result.data
                        updateState {
                            copy(
                                isLoading = false,
                                isLoadingMore = false,
                                isPullRefreshing = false,
                                // TMDB uses offset pagination on a live-ranked list, so
                                // popularity shifts between requests can place the same
                                // movie on two consecutive pages. Dedup by ID to prevent
                                // LazyVerticalGrid from crashing on a repeated key.
                                movies = if (replace) paged.items else {
                                    val seen = movies.mapTo(HashSet()) { it.id }
                                    movies + paged.items.filter { it.id !in seen }
                                },
                                error = null,
                                currentPage = page,
                                totalPages = paged.totalPages,
                            )
                        }
                    }
                    is Result.Error -> {
                        if (uiState.value.movies.isNotEmpty()) {
                            updateState { copy(isLoadingMore = false, isPullRefreshing = false) }
                            if (replace) sendEvent(HomeEvent.ShowSnackbar(UiText.StringResource(R.string.error_refresh_cached)))
                        } else {
                            updateState {
                                copy(
                                    isLoading = false,
                                    isLoadingMore = false,
                                    isPullRefreshing = false,
                                    error = UiText.StringResource(CoreUiR.string.error_something_went_wrong),
                                )
                            }
                        }
                    }
                    Result.Loading -> Unit
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
                loadPage(page = 1, replace = true)
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
                            copy(genres = result.data, isGenresLoading = false, selectedGenreId = firstId)
                        }
                        if (firstId != null) loadPage(page = 1, replace = true)
                    }
                    is Result.Error -> updateState { copy(isGenresLoading = false, isLoading = false) }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
