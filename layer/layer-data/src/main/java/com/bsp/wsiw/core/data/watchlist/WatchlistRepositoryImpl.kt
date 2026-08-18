package com.bsp.wsiw.core.data.watchlist

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.data.remote.model.MovieDto
import com.bsp.wsiw.core.data.remote.model.WatchlistUpdateBody
import com.bsp.wsiw.core.data.util.safeApiCall
import com.bsp.wsiw.core.database.dao.WatchlistDao
import com.bsp.wsiw.core.database.entity.WatchlistEntity
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"
private const val MAX_PAGES = 5

class WatchlistRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
    private val dao: WatchlistDao,
    private val sessionRepository: SessionRepository,
) : WatchlistRepository {

    override fun getWatchlist(): Flow<List<Movie>> =
        dao.getAll().map { entities -> entities.map(WatchlistEntity::toMovie) }

    override fun isWatchlisted(movieId: Int): Flow<Boolean> = dao.isWatchlisted(movieId)

    override suspend fun addToWatchlist(movie: Movie) {
        withContext(Dispatchers.IO) { dao.insert(movie.toEntity()) }
        val accountId = sessionRepository.accountId.firstOrNull() ?: return
        try {
            apiService.updateWatchlist(accountId, WatchlistUpdateBody(mediaId = movie.id, watchlist = true))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    override suspend fun removeFromWatchlist(movieId: Int) {
        withContext(Dispatchers.IO) { dao.deleteById(movieId) }
        val accountId = sessionRepository.accountId.firstOrNull() ?: return
        try {
            apiService.updateWatchlist(accountId, WatchlistUpdateBody(mediaId = movieId, watchlist = false))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    override suspend fun refreshWatchlist() {
        val accountId = sessionRepository.accountId.firstOrNull() ?: return
        val movies = mutableListOf<MovieDto>()
        var page = 1
        do {
            val result = safeApiCall { apiService.getWatchlistMovies(accountId, page) }
            if (result is Result.Error) return
            val response = (result as Result.Success).data
            movies.addAll(response.results)
            if (page >= response.totalPages || page >= MAX_PAGES) break
            page++
        } while (true)
        val now = System.currentTimeMillis()
        val entities = movies.mapIndexed { index, dto ->
            dto.toWatchlistEntity(addedAt = now - index * 1_000L)
        }
        withContext(Dispatchers.IO) { dao.deleteAll(); dao.insertAll(entities) }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) { dao.deleteAll() }
    }
}

private fun WatchlistEntity.toMovie() = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    addedAt = addedAt,
)

private fun Movie.toEntity() = WatchlistEntity(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    addedAt = if (addedAt > 0L) addedAt else System.currentTimeMillis(),
)

private fun MovieDto.toWatchlistEntity(addedAt: Long) = WatchlistEntity(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE_URL + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE_URL + it },
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    addedAt = addedAt,
)
