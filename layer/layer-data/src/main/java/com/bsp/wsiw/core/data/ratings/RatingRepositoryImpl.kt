package com.bsp.wsiw.core.data.ratings

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.data.movie.toDomain
import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.data.remote.model.MovieDto
import com.bsp.wsiw.core.data.remote.model.RatingBody
import com.bsp.wsiw.core.data.util.safeApiCall
import com.bsp.wsiw.core.database.dao.MovieDetailCacheDao
import com.bsp.wsiw.core.database.dao.RatingDao
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import com.bsp.wsiw.core.database.entity.RatingEntity
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.repository.RatingRepository
import com.bsp.wsiw.core.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val MAX_PAGES = 5

class RatingRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
    private val dao: RatingDao,
    private val movieDetailDao: MovieDetailCacheDao,
    private val sessionRepository: SessionRepository,
) : RatingRepository {

    override fun getMovieRating(movieId: Int): Flow<Float?> = dao.getRating(movieId)

    override fun getAllRatings(): Flow<Map<Int, Float>> =
        dao.getAll().map { entities -> entities.associate { it.movieId to it.rating } }

    override fun getLocalRatedMovies(): Flow<List<Pair<Movie, Float>>> =
        dao.getAll()
            .map { entities ->
                entities
                    .sortedByDescending { it.ratedAt }
                    .mapNotNull { entity ->
                        val detail = movieDetailDao.getByIdSync(entity.movieId) ?: return@mapNotNull null
                        detail.toMovie() to entity.rating
                    }
            }
            .flowOn(Dispatchers.IO)

    override suspend fun rateMovie(movieId: Int, rating: Float) {
        withContext(Dispatchers.IO) { dao.upsert(RatingEntity(movieId, rating, System.currentTimeMillis())) }
        try { apiService.rateMovie(movieId, RatingBody(rating)) } catch (_: Exception) { }
    }

    override suspend fun removeRating(movieId: Int) {
        withContext(Dispatchers.IO) { dao.delete(movieId) }
        try { apiService.deleteMovieRating(movieId) } catch (_: Exception) { }
    }

    override suspend fun refreshRatings() {
        val accountId = sessionRepository.accountId.firstOrNull() ?: return
        val movies = mutableListOf<MovieDto>()
        var page = 1
        do {
            val result = safeApiCall { apiService.getRatedMovies(accountId, page) }
            if (result is Result.Error) return
            val response = (result as Result.Success).data
            movies.addAll(response.results)
            if (page >= response.totalPages || page >= MAX_PAGES) break
            page++
        } while (true)
        val now = System.currentTimeMillis()
        val entities = movies.mapIndexedNotNull { index, dto ->
            val rating = dto.rating ?: return@mapIndexedNotNull null
            RatingEntity(movieId = dto.id, rating = rating, ratedAt = now - index * 1_000L)
        }
        val detailSnapshots = movies.map { it.toMinimalDetailEntity() }
        withContext(Dispatchers.IO) {
            dao.deleteAll()
            dao.insertAll(entities)
            movieDetailDao.insertAllIfAbsent(detailSnapshots)
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) { dao.deleteAll() }
    }

    override suspend fun getRatedMoviesPage(accountId: Int, page: Int): PagedResult<Pair<Movie, Float>> =
        withContext(Dispatchers.IO) {
            val response = apiService.getRatedMovies(accountId, page)
            val items = response.results.mapNotNull { dto ->
                val rating = dto.rating ?: return@mapNotNull null
                dto.toDomain() to rating
            }
            PagedResult(items = items, totalPages = response.totalPages)
        }

    override suspend fun fetchRatedMovies(accountId: Int): List<Pair<Movie, Float>> =
        withContext(Dispatchers.IO) {
            apiService.getRatedMovies(accountId, page = 1).results.mapNotNull { dto ->
                val rating = dto.rating ?: return@mapNotNull null
                dto.toDomain() to rating
            }
        }
}

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"

private fun MovieDto.toMinimalDetailEntity() = MovieDetailEntity(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE + it },
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    tagline = "",
    genres = emptyList(),
    runtime = 0,
    originalLanguage = originalLanguage,
    cachedAt = System.currentTimeMillis(),
)

private fun MovieDetailEntity.toMovie() = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
)
