package com.bsp.wsiw.feature.profile

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.usecase.GetLocalRatedMoviesUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatedMoviesViewModel @Inject constructor(
    private val getLocalRatedMovies: GetLocalRatedMoviesUseCase,
) : BaseViewModel<RatedMoviesAction, RatedMoviesEvent, RatedMoviesUiState>(
    initialState = RatedMoviesUiState(),
) {
    init {
        viewModelScope.launch {
            getLocalRatedMovies().collect { movies ->
                updateState { copy(movies = movies, isLoading = false) }
            }
        }
    }

    override fun handleAction(action: RatedMoviesAction) = Unit
}
