package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.data.util.safeApiCall
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.network.TmdbApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
) : MovieRepository {

    override fun getPopularMovies(page: Int): Flow<Result<List<Movie>>> = flow {
        emit(safeApiCall { apiService.getPopularMovies(page).results.map { it.toDomain() } })
    }

    override fun searchMovies(query: String, page: Int): Flow<Result<List<Movie>>> = flow {
        emit(safeApiCall { apiService.searchMovies(query, page).results.map { it.toDomain() } })
    }
}
