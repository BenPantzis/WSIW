package com.bsp.wsiw.core.data.ratings

import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.data.remote.model.RatingBody
import com.bsp.wsiw.core.database.dao.RatingDao
import com.bsp.wsiw.core.database.entity.RatingEntity
import com.bsp.wsiw.core.domain.repository.RatingRepository
import com.bsp.wsiw.core.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val MAX_PAGES = 5

class RatingRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
    private val dao: RatingDao,
    private val sessionRepository: SessionRepository,
) : RatingRepository {

    override fun getMovieRating(movieId: Int): Flow<Float?> = dao.getRating(movieId)

    override fun getAllRatings(): Flow<Map<Int, Float>> =
        dao.getAll().map { entities -> entities.associate { it.movieId to it.rating } }

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
        val movies = buildList {
            var page = 1
            do {
                val response = apiService.getRatedMovies(accountId, page)
                addAll(response.results)
                if (page >= response.totalPages || page >= MAX_PAGES) break
                page++
            } while (true)
        }
        val now = System.currentTimeMillis()
        val entities = movies.mapIndexedNotNull { index, dto ->
            val rating = dto.rating ?: return@mapIndexedNotNull null
            RatingEntity(movieId = dto.id, rating = rating, ratedAt = now - index * 1_000L)
        }
        withContext(Dispatchers.IO) {
            dao.deleteAll()
            dao.insertAll(entities)
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) { dao.deleteAll() }
    }
}
