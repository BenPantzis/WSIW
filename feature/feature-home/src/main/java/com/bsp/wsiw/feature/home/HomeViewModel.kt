package com.bsp.wsiw.feature.home

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.usecase.GetPopularMoviesUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMovies: GetPopularMoviesUseCase,
) : BaseViewModel<HomeAction, HomeEvent, HomeUiState>(
    initialState = HomeUiState(),
) {
    init {
        onAction(HomeAction.LoadMovies)
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            HomeAction.LoadMovies -> loadMovies()
            HomeAction.Retry -> loadMovies()
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            getPopularMovies(1).collect { result ->
                when (result) {
                    is Result.Success -> updateState { copy(isLoading = false, movies = result.data) }
                    is Result.Error -> updateState {
                        copy(isLoading = false, error = result.exception?.message ?: "Something went wrong")
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }
}

sealed interface HomeAction {
    data object LoadMovies : HomeAction
    data object Retry : HomeAction
}

sealed interface HomeEvent {
    data class ShowSnackbar(val message: String) : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
)
