package com.bsp.wsiw.core.data.tv

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.data.util.safeApiCall
import com.bsp.wsiw.core.domain.model.CastMember
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.Season
import com.bsp.wsiw.core.domain.model.SortBy
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.model.TvShowDetail
import com.bsp.wsiw.core.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"
private const val PROFILE_BASE = "https://image.tmdb.org/t/p/w185"

class TvRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
) : TvRepository {

    override fun getTvByCategory(category: String, page: Int): Flow<Result<PagedResult<TvShow>>> = flow {
        emit(safeApiCall {
            val response = when (category) {
                "trending" -> apiService.getTrendingTv(page)
                "popular" -> apiService.getPopularTv(page)
                "top_rated" -> apiService.getTopRatedTv(page)
                "on_the_air" -> apiService.getOnTheAirTv(page)
                "airing_today" -> apiService.getAiringTodayTv(page)
                else -> throw IllegalArgumentException("Unknown TV category: $category")
            }
            PagedResult(items = response.results.map { it.toDomain() }, totalPages = response.totalPages)
        })
    }

    override fun discoverTv(genreId: Int?, filter: DiscoverFilter, page: Int): Flow<Result<PagedResult<TvShow>>> = flow {
        emit(safeApiCall {
            val minVoteCount = if (filter.sortBy == SortBy.Rating) 100 else null
            val response = apiService.discoverTv(
                genreId = genreId,
                sortBy = filter.sortBy.apiValue,
                minRating = filter.minRating,
                year = filter.year,
                minVoteCount = minVoteCount,
                page = page,
            )
            PagedResult(items = response.results.map { it.toDomain() }, totalPages = response.totalPages)
        })
    }

    override fun getTvGenres(): Flow<Result<List<Genre>>> = flow {
        emit(safeApiCall { apiService.getTvGenres().genres.map { Genre(id = it.id, name = it.name) } })
    }

    override fun getTvDetail(seriesId: Int): Flow<Result<TvShowDetail>> = flow {
        emit(safeApiCall {
            val dto = apiService.getTvDetail(seriesId)

            val countryCode = Locale.getDefault().country.ifEmpty { "US" }
            val contentRating = dto.contentRatings?.results
                ?.firstOrNull { it.country == countryCode }?.rating
                ?: dto.contentRatings?.results?.firstOrNull { it.country == "US" }?.rating

            val trailerKey = dto.videos?.results
                ?.filter { it.site == "YouTube" && it.type == "Trailer" }
                ?.maxByOrNull { if (it.official) 1 else 0 }
                ?.key

            TvShowDetail(
                id = dto.id,
                name = dto.name,
                overview = dto.overview,
                tagline = dto.tagline.orEmpty(),
                posterUrl = dto.posterPath?.let { POSTER_BASE + it },
                backdropUrl = dto.backdropPath?.let { BACKDROP_BASE + it },
                firstAirDate = dto.firstAirDate,
                voteAverage = dto.voteAverage,
                voteCount = dto.voteCount,
                status = dto.status,
                genres = dto.genres.map { Genre(id = it.id, name = it.name) },
                seasons = dto.seasons
                    .filter { it.seasonNumber > 0 }
                    .map { s ->
                        Season(
                            seasonNumber = s.seasonNumber,
                            name = s.name,
                            episodeCount = s.episodeCount,
                            airDate = s.airDate,
                            posterUrl = s.posterPath?.let { POSTER_BASE + it },
                            overview = s.overview.orEmpty(),
                        )
                    },
                networks = dto.networks.map { it.name },
                contentRating = contentRating,
                cast = dto.aggregateCredits?.cast
                    ?.filter { it.profilePath != null }
                    ?.take(20)
                    ?.map { member ->
                        CastMember(
                            id = member.id,
                            name = member.name,
                            character = member.roles.firstOrNull()?.character ?: "",
                            profileUrl = member.profilePath?.let { PROFILE_BASE + it },
                        )
                    } ?: emptyList(),
                similar = dto.similar?.results?.map { it.toDomain() } ?: emptyList(),
                recommendations = dto.recommendations?.results?.map { it.toDomain() } ?: emptyList(),
                trailerKey = trailerKey,
            )
        })
    }
}
