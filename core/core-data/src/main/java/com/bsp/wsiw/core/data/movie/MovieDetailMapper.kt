package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.data.remote.model.CastMemberDto
import com.bsp.wsiw.core.data.remote.model.GenreDto
import com.bsp.wsiw.core.data.remote.model.MovieDetailDto
import com.bsp.wsiw.core.data.remote.model.ReleaseDatesWrapperDto
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import com.bsp.wsiw.core.domain.model.CastMember
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.VideoEntry

private const val POSTER_BASE_URL_W500    = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL_W1280 = "https://image.tmdb.org/t/p/w1280"
private const val PROFILE_BASE_URL_W185   = "https://image.tmdb.org/t/p/w185"

fun MovieDetailDto.toDomain() = MovieDetail(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE_URL_W500 + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE_URL_W1280 + it },
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.map { it.toDomain() },
    runtime = runtime,
    originalLanguage = originalLanguage,
    trailer = videos?.results
        ?.filter { it.site == "YouTube" && it.type == "Trailer" }
        ?.let { trailers -> trailers.firstOrNull { it.official } ?: trailers.firstOrNull() }
        ?.let { VideoEntry(key = it.key, name = it.name) },
    cast = credits?.cast
        ?.sortedBy { it.order }
        ?.take(10)
        ?.map { it.toDomain() }
        ?: emptyList(),
    similarMovies = similar?.results
        ?.take(10)
        ?.map { it.toDomain() }
        ?: emptyList(),
    recommendedMovies = recommendations?.results
        ?.take(10)
        ?.map { it.toDomain() }
        ?: emptyList(),
    certification = extractCertification(releaseDates),
)

private fun extractCertification(releaseDates: ReleaseDatesWrapperDto?): String? {
    val usEntries = releaseDates?.results
        ?.firstOrNull { it.country == "US" }
        ?.releaseDates
        ?: return null
    return usEntries.firstOrNull { it.type == 3 && it.certification.isNotBlank() }?.certification
        ?: usEntries.firstOrNull { it.certification.isNotBlank() }?.certification
}

private fun GenreDto.toDomain() = Genre(id = id, name = name)

private fun CastMemberDto.toDomain() = CastMember(
    id = id,
    name = name,
    character = character,
    profileUrl = profilePath?.let { PROFILE_BASE_URL_W185 + it },
)

fun MovieDetailDto.toEntity() = MovieDetailEntity(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE_URL_W500 + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE_URL_W1280 + it },
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    tagline = tagline,
    genres = genres.map { Genre(id = it.id, name = it.name) },
    runtime = runtime,
    originalLanguage = originalLanguage,
    trailerKey = videos?.results
        ?.filter { it.site == "YouTube" && it.type == "Trailer" }
        ?.let { t -> t.firstOrNull { it.official } ?: t.firstOrNull() }
        ?.key,
    trailerName = videos?.results
        ?.filter { it.site == "YouTube" && it.type == "Trailer" }
        ?.let { t -> t.firstOrNull { it.official } ?: t.firstOrNull() }
        ?.name,
    cast = credits?.cast?.sortedBy { it.order }?.take(10)?.map { it.toDomain() } ?: emptyList(),
    similarMovies = similar?.results?.take(10)?.map { it.toDomain() } ?: emptyList(),
    recommendedMovies = recommendations?.results?.take(10)?.map { it.toDomain() } ?: emptyList(),
    certification = extractCertification(releaseDates),
    cachedAt = System.currentTimeMillis(),
)
