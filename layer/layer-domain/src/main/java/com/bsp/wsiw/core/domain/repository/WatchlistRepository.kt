package com.bsp.wsiw.core.domain.repository

import com.bsp.wsiw.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlist(): Flow<List<Movie>>
    fun isWatchlisted(movieId: Int): Flow<Boolean>
    suspend fun addToWatchlist(movie: Movie)
    suspend fun removeFromWatchlist(movieId: Int)
    suspend fun refreshWatchlist()
    suspend fun clearAll()
}
