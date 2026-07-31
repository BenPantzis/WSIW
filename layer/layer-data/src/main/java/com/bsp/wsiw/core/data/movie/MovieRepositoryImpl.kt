package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.data.util.networkBoundResource
import com.bsp.wsiw.core.data.util.safeApiCall
import com.bsp.wsiw.core.database.dao.MovieDetailCacheDao
import com.bsp.wsiw.core.database.dao.PopularMovieCacheDao
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.PersonDetail
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.domain.model.WatchProvider
import com.bsp.wsiw.core.domain.model.WatchProviders
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

    override fun getMoviesByCategory(category: String, page: Int): Flow<Result<PagedResult<Movie>>> = flow {
        emit(safeApiCall {
            val response = when (category) {
                "popular" -> apiService.getPopularMovies(page)
                "trending" -> apiService.getTrendingMovies(page)
                "top_rated" -> apiService.getTopRatedMovies(page)
                "now_playing" -> apiService.getNowPlayingMovies(page)
                "upcoming" -> apiService.getUpcomingMovies(page)
                else -> throw IllegalArgumentException("Unknown category: $category")
            }
            PagedResult(items = response.results.map { it.toDomain() }, totalPages = response.totalPages)
        })
    }

    override fun getGenres(): Flow<Result<List<Genre>>> = flow {
        emit(safeApiCall { apiService.getGenres().genres.map { Genre(id = it.id, name = it.name) } })
    }

    override fun discoverMovies(genreId: Int, page: Int): Flow<Result<PagedResult<Movie>>> = flow {
        emit(safeApiCall {
            val response = apiService.discoverMovies(genreId, page)
            PagedResult(items = response.results.map { it.toDomain() }, totalPages = response.totalPages)
        })
    }

    override fun searchMovies(query: String, page: Int): Flow<Result<List<Movie>>> = flow {
        emit(safeApiCall { apiService.searchMovies(query, page).results.map { it.toDomain() } })
    }

    override fun getPersonDetail(personId: Int): Flow<Result<PersonDetail>> = flow {
        emit(safeApiCall {
            val dto = apiService.getPersonDetail(personId)
            PersonDetail(
                id = dto.id,
                name = dto.name,
                biography = dto.biography,
                birthday = dto.birthday,
                placeOfBirth = dto.placeOfBirth,
                profileUrl = dto.profilePath?.let { "https://image.tmdb.org/t/p/w342$it" },
                knownForDepartment = dto.knownForDepartment,
                filmography = dto.movieCredits?.cast
                    ?.filter { it.posterPath != null }
                    ?.sortedByDescending { it.voteAverage }
                    ?.take(20)
                    ?.map { it.toDomain() }
                    ?: emptyList(),
            )
        })
    }

    override fun getMovieReviews(movieId: Int, page: Int): Flow<Result<PagedResult<Review>>> = flow {
        emit(safeApiCall {
            val response = apiService.getMovieReviews(movieId, page)
            PagedResult(
                items = response.results.map { dto ->
                    Review(
                        id = dto.id,
                        author = dto.author,
                        avatarUrl = dto.authorDetails.avatarPath?.let {
                            val path = if (it.startsWith("/https")) it.removePrefix("/") else it
                            "https://image.tmdb.org/t/p/w185$path"
                        },
                        rating = dto.authorDetails.rating,
                        content = dto.content,
                        createdAt = dto.createdAt,
                    )
                },
                totalPages = response.totalPages,
            )
        })
    }

    override fun getWatchProviders(movieId: Int): Flow<Result<WatchProviders>> = flow {
        emit(safeApiCall {
            val response = apiService.getMovieWatchProviders(movieId)
            val region = java.util.Locale.getDefault().country.ifEmpty { "US" }
            val country = response.results[region] ?: response.results["US"]
            WatchProviders(
                streaming = country?.flatrate?.sortedBy { it.displayPriority }?.take(5)
                    ?.mapNotNull { it.logoPath?.let { path -> WatchProvider(it.providerName, "https://image.tmdb.org/t/p/original$path") } }
                    ?: emptyList(),
                rent = country?.rent?.sortedBy { it.displayPriority }?.take(4)
                    ?.mapNotNull { it.logoPath?.let { path -> WatchProvider(it.providerName, "https://image.tmdb.org/t/p/original$path") } }
                    ?: emptyList(),
                buy = country?.buy?.sortedBy { it.displayPriority }?.take(4)
                    ?.mapNotNull { it.logoPath?.let { path -> WatchProvider(it.providerName, "https://image.tmdb.org/t/p/original$path") } }
                    ?: emptyList(),
            )
        })
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

    override suspend fun getFavoriteCount(accountId: Int): Int = try {
        apiService.getFavoriteMovies(accountId, page = 1).totalResults
    } catch (_: Exception) { 0 }
}
