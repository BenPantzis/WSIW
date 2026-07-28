package com.bsp.wsiw.core.database.watchlist

import com.bsp.wsiw.core.database.dao.WatchlistDao
import com.bsp.wsiw.core.database.entity.WatchlistEntity
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WatchlistRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun getWatchlist(): Flow<List<Movie>> =
        dao.getAll().map { entities -> entities.map(WatchlistEntity::toMovie) }

    override fun isWatchlisted(movieId: Int): Flow<Boolean> = dao.isWatchlisted(movieId)

    override suspend fun addToWatchlist(movie: Movie) = withContext(Dispatchers.IO) { dao.insert(movie.toEntity()); Unit }

    override suspend fun removeFromWatchlist(movieId: Int): Int = withContext(Dispatchers.IO) { dao.deleteById(movieId) }
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
)
