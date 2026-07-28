package com.bsp.wsiw.feature.search

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
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
) : BaseViewModel<SearchAction, SearchEvent, SearchUiState>(
    initialState = SearchUiState(),
) {
    // Internal debounce gate — not exposed; query truth lives in uiState
    private val _query = MutableStateFlow("")

    init {
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
                    // Propagate search outcome only — query is already set by handleAction
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
                // Immediate update so the text field never lags; clears stale results
                updateState { copy(query = action.query, isLoading = false, movies = emptyList(), error = null) }
                _query.value = action.query
            }
            SearchAction.ClearQuery -> {
                updateState { SearchUiState() }
                _query.value = ""
            }
        }
    }
}
