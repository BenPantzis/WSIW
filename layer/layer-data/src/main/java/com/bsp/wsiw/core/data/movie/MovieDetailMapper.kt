package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.data.remote.model.MovieDetailDto
import com.bsp.wsiw.core.data.remote.model.ReleaseDatesWrapperDto
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import com.bsp.wsiw.core.database.model.CastMemberData
import com.bsp.wsiw.core.database.model.GenreData
import com.bsp.wsiw.core.database.model.MovieData
import com.bsp.wsiw.core.domain.model.CastMember
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.VideoEntry

private const val POSTER_BASE_URL_W500    = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL_W1280 = "https://image.tmdb.org/t/p/w1280"
private const val BACKDROP_BASE_URL_W780  = "https://image.tmdb.org/t/p/w780"
private const val PROFILE_BASE_URL_W185   = "https://image.tmdb.org/t/p/w185"

// DTO → Domain
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
    genres = genres.map { Genre(id = it.id, name = it.name) },
    runtime = runtime,
    originalLanguage = originalLanguage,
    trailer = videos?.results
        ?.filter { it.site == "YouTube" && it.type == "Trailer" }
        ?.let { trailers -> trailers.firstOrNull { it.official } ?: trailers.firstOrNull() }
        ?.let { VideoEntry(key = it.key, name = it.name) },
    cast = credits?.cast
        ?.sortedBy { it.order }
        ?.take(10)
        ?.map { CastMember(id = it.id, name = it.name, character = it.character, profileUrl = it.profilePath?.let { p -> PROFILE_BASE_URL_W185 + p }) }
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

// Entity → Domain
fun MovieDetailEntity.toDomain() = MovieDetail(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.map { Genre(id = it.id, name = it.name) },
    runtime = runtime,
    originalLanguage = originalLanguage,
    trailer = trailerKey?.let { key -> trailerName?.let { name -> VideoEntry(key, name) } },
    cast = cast.map { CastMember(id = it.id, name = it.name, character = it.character, profileUrl = it.profileUrl) },
    similarMovies = similarMovies.map { Movie(id = it.id, title = it.title, overview = it.overview, posterUrl = it.posterUrl, backdropUrl = it.backdropUrl, releaseDate = it.releaseDate, voteAverage = it.voteAverage, voteCount = it.voteCount) },
    recommendedMovies = recommendedMovies.map { Movie(id = it.id, title = it.title, overview = it.overview, posterUrl = it.posterUrl, backdropUrl = it.backdropUrl, releaseDate = it.releaseDate, voteAverage = it.voteAverage, voteCount = it.voteCount) },
    certification = certification,
)

// DTO → Entity
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
    genres = genres.map { GenreData(id = it.id, name = it.name) },
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
    cast = credits?.cast?.sortedBy { it.order }?.take(10)?.map {
        CastMemberData(id = it.id, name = it.name, character = it.character, profileUrl = it.profilePath?.let { p -> PROFILE_BASE_URL_W185 + p })
    } ?: emptyList(),
    similarMovies = similar?.results?.take(10)?.map {
        MovieData(id = it.id, title = it.title, overview = it.overview, posterUrl = it.posterPath?.let { p -> POSTER_BASE_URL_W500 + p }, backdropUrl = it.backdropPath?.let { p -> BACKDROP_BASE_URL_W780 + p }, releaseDate = it.releaseDate, voteAverage = it.voteAverage, voteCount = it.voteCount)
    } ?: emptyList(),
    recommendedMovies = recommendations?.results?.take(10)?.map {
        MovieData(id = it.id, title = it.title, overview = it.overview, posterUrl = it.posterPath?.let { p -> POSTER_BASE_URL_W500 + p }, backdropUrl = it.backdropPath?.let { p -> BACKDROP_BASE_URL_W780 + p }, releaseDate = it.releaseDate, voteAverage = it.voteAverage, voteCount = it.voteCount)
    } ?: emptyList(),
    certification = extractCertification(releaseDates),
    cachedAt = System.currentTimeMillis(),
)

private fun extractCertification(releaseDates: ReleaseDatesWrapperDto?): String? {
    val usEntries = releaseDates?.results
        ?.firstOrNull { it.country == "US" }
        ?.releaseDates
        ?: return null
    return usEntries.firstOrNull { it.type == 3 && it.certification.isNotBlank() }?.certification
        ?: usEntries.firstOrNull { it.certification.isNotBlank() }?.certification
}
