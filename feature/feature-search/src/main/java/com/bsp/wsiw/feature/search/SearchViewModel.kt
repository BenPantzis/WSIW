package com.bsp.wsiw.feature.search

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.domain.usecase.SearchMoviesUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMovies: SearchMoviesUseCase,
    private val movieRepository: MovieRepository,
) : BaseViewModel<SearchAction, SearchEvent, SearchUiState>(
    initialState = SearchUiState(),
) {
    private val _query = MutableStateFlow("")

    init {
        loadTrending()
        viewModelScope.launch {
            _query
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(SearchUiState(query = query))
                    } else {
                        searchMovies(query).map { result ->
                            when (result) {
                                Result.Loading -> SearchUiState(query = query, isLoading = true)
                                is Result.Success -> SearchUiState(query = query, movies = result.data)
                                is Result.Error -> SearchUiState(
                                    query = query,
                                    error = result.exception?.message ?: "Something went wrong",
                                )
                            }
                        }
                    }
                }
                .collect { searchResult ->
                    updateState {
                        copy(
                            isLoading = searchResult.isLoading,
                            movies = searchResult.movies,
                            error = searchResult.error,
                        )
                    }
                }
        }
    }

    override fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.UpdateQuery -> {
                updateState { copy(query = action.query, isLoading = false, movies = emptyList(), error = null) }
                _query.value = action.query
            }
            SearchAction.ClearQuery -> {
                updateState { copy(query = "", isLoading = false, movies = emptyList(), error = null) }
                _query.value = ""
            }
        }
    }

    private fun loadTrending() {
        viewModelScope.launch {
            movieRepository.getMoviesByCategory("trending").collect { result ->
                when (result) {
                    is Result.Success -> updateState {
                        copy(trendingMovies = result.data.items, isTrendingLoading = false)
                    }
                    is Result.Error -> updateState { copy(isTrendingLoading = false) }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
