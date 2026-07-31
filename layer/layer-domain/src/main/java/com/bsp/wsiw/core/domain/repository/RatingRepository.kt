package com.bsp.wsiw.core.domain.repository

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.PagedResult
import kotlinx.coroutines.flow.Flow

interface RatingRepository {
    fun getMovieRating(movieId: Int): Flow<Float?>
    fun getAllRatings(): Flow<Map<Int, Float>>
    suspend fun rateMovie(movieId: Int, rating: Float)
    suspend fun removeRating(movieId: Int)
    suspend fun refreshRatings()
    suspend fun clearAll()
    fun getLocalRatedMovies(): Flow<List<Pair<Movie, Float>>>
    suspend fun fetchRatedMovies(accountId: Int): List<Pair<Movie, Float>>
    suspend fun getRatedMoviesPage(accountId: Int, page: Int): PagedResult<Pair<Movie, Float>>
}
