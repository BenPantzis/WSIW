package com.bsp.wsiw.feature.tv

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.repository.TvRepository
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.R as CoreUiR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvListViewModel @Inject constructor(
    private val tvRepository: TvRepository,
) : BaseViewModel<TvListAction, TvListEvent, TvListUiState>(
    initialState = TvListUiState(),
) {
    private var loadJob: Job? = null

    init {
        onAction(TvListAction.LoadShows)
    }

    override fun handleAction(action: TvListAction) {
        when (action) {
            TvListAction.LoadShows -> loadPage(page = 1, replace = true)
            TvListAction.Retry -> loadPage(page = 1, replace = true)
            TvListAction.Refresh -> {
                updateState { copy(isPullRefreshing = true) }
                loadPage(page = 1, replace = true)
            }
            TvListAction.LoadNextPage -> {
                val state = uiState.value
                if (state.canLoadMore) loadPage(page = state.currentPage + 1, replace = false)
            }
            is TvListAction.SelectCategory -> {
                if (uiState.value.selectedCategory == action.category) return
                updateState {
                    copy(
                        selectedCategory = action.category,
                        shows = emptyList(),
                        error = null,
                        isLoading = true,
                        currentPage = 1,
                        totalPages = Int.MAX_VALUE,
                        filter = DiscoverFilter(sortBy = action.category.defaultSortBy),
                    )
                }
                if (action.category == TvCategory.ByGenre) {
                    loadGenresIfNeeded()
                } else {
                    loadPage(page = 1, replace = true)
                }
            }
            is TvListAction.SelectGenre -> {
                updateState {
                    copy(selectedGenreId = action.genreId, shows = emptyList(), isLoading = true, currentPage = 1, totalPages = Int.MAX_VALUE)
                }
                loadPage(page = 1, replace = true)
            }
            TvListAction.OpenFilterSheet -> updateState { copy(showFilterSheet = true) }
            TvListAction.DismissFilterSheet -> updateState { copy(showFilterSheet = false) }
            is TvListAction.ApplyFilter -> {
                updateState {
                    copy(
                        filter = action.filter,
                        showFilterSheet = false,
                        shows = emptyList(),
                        isLoading = true,
                        currentPage = 1,
                        totalPages = Int.MAX_VALUE,
                    )
                }
                loadPage(page = 1, replace = true)
            }
        }
    }

    private fun pageSource(page: Int): Flow<Result<PagedResult<TvShow>>> {
        val state = uiState.value
        return when {
            state.selectedCategory == TvCategory.ByGenre -> {
                val genreId = state.selectedGenreId ?: return flow { }
                tvRepository.discoverTv(genreId = genreId, filter = state.filter, page = page)
            }
            !state.filter.isDefault -> tvRepository.discoverTv(
                genreId = null,
                filter = state.filter,
                page = page,
            )
            else -> tvRepository.getTvByCategory(state.selectedCategory.apiKey, page)
        }
    }

    private fun loadPage(page: Int, replace: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (replace) {
                updateState { copy(isLoading = shows.isEmpty(), isLoadingMore = false) }
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
                                shows = if (replace) paged.items else {
                                    val seen = shows.mapTo(HashSet()) { it.id }
                                    shows + paged.items.filter { it.id !in seen }
                                },
                                error = null,
                                currentPage = page,
                                totalPages = paged.totalPages,
                            )
                        }
                    }
                    is Result.Error -> {
                        if (uiState.value.shows.isNotEmpty()) {
                            updateState { copy(isLoadingMore = false, isPullRefreshing = false) }
                            if (replace) sendEvent(TvListEvent.ShowSnackbar(UiText.StringResource(R.string.error_refresh_cached)))
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
            tvRepository.getTvGenres().collect { result ->
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
