package com.bsp.wsiw.feature.profile

import com.bsp.wsiw.core.domain.model.Movie

sealed interface RatedMoviesAction

sealed interface RatedMoviesEvent

data class RatedMoviesUiState(
    val movies: List<Pair<Movie, Float>> = emptyList(),
    val isLoading: Boolean = true,
)
