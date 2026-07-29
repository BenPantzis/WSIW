package com.bsp.wsiw.core.domain.repository

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getPopularMovies(page: Int = 1, forceRefresh: Boolean = false): Flow<Result<List<Movie>>>
    fun getMoviesByCategory(category: String, page: Int = 1): Flow<Result<List<Movie>>>
    fun getGenres(): Flow<Result<List<Genre>>>
    fun discoverMovies(genreId: Int, page: Int = 1): Flow<Result<List<Movie>>>
    fun searchMovies(query: String, page: Int = 1): Flow<Result<List<Movie>>>
    fun getMovieDetail(movieId: Int): Flow<Result<MovieDetail>>
}
