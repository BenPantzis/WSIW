package com.bsp.wsiw.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface RatingRepository {
    fun getMovieRating(movieId: Int): Flow<Float?>
    fun getAllRatings(): Flow<Map<Int, Float>>
    suspend fun rateMovie(movieId: Int, rating: Float)
    suspend fun removeRating(movieId: Int)
    suspend fun refreshRatings()
    suspend fun clearAll()
}
