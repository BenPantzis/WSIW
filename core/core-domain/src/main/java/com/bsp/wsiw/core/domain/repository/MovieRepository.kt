package com.bsp.wsiw.core.domain.repository

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getPopularMovies(page: Int = 1): Flow<Result<List<Movie>>>
    fun searchMovies(query: String, page: Int = 1): Flow<Result<List<Movie>>>
}
