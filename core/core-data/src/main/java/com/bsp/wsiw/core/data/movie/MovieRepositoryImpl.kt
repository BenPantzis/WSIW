package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.data.util.networkBoundResource
import com.bsp.wsiw.core.data.util.safeApiCall
import com.bsp.wsiw.core.database.dao.MovieDetailCacheDao
import com.bsp.wsiw.core.database.dao.PopularMovieCacheDao
import com.bsp.wsiw.core.database.entity.toDomain
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val CACHE_TTL = 10 * 60 * 1000L // 10 minutes

class MovieRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
    private val popularMovieDao: PopularMovieCacheDao,
    private val movieDetailDao: MovieDetailCacheDao,
) : MovieRepository {

    override fun getPopularMovies(page: Int, forceRefresh: Boolean): Flow<Result<List<Movie>>> =
        networkBoundResource(
            query = { popularMovieDao.getByPage(page) },
            fetch = { apiService.getPopularMovies(page).results },
            saveFetchResult = { dtos ->
                popularMovieDao.deleteByPage(page)
                popularMovieDao.insertAll(dtos.map { it.toEntity(page) })
            },
            shouldFetch = { cached ->
                forceRefresh || cached.isEmpty() || System.currentTimeMillis() - cached.minOf { it.cachedAt } > CACHE_TTL
            },
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data.map { it.toDomain() })
                is Result.Error -> Result.Error(result.exception)
                Result.Loading -> Result.Loading
            }
        }

    override fun searchMovies(query: String, page: Int): Flow<Result<List<Movie>>> = flow {
        emit(safeApiCall { apiService.searchMovies(query, page).results.map { it.toDomain() } })
    }

    override fun getMovieDetail(movieId: Int): Flow<Result<MovieDetail>> =
        networkBoundResource(
            query = { movieDetailDao.getById(movieId) },
            fetch = { apiService.getMovieDetail(movieId) },
            saveFetchResult = { dto -> movieDetailDao.insert(dto.toEntity()) },
            shouldFetch = { cached ->
                cached == null || System.currentTimeMillis() - cached.cachedAt > CACHE_TTL
            },
        ).map { result ->
            when (result) {
                is Result.Success -> result.data?.let { Result.Success(it.toDomain(), result.isRefreshing) }
                    ?: if (result.isRefreshing) Result.Loading  // cache miss while fetch is in-flight
                       else Result.Error(Exception("Movie not found in cache"))
                is Result.Error -> Result.Error(result.exception)
                Result.Loading -> Result.Loading
            }
        }
}
