package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.data.remote.model.GenreDto
import com.bsp.wsiw.core.data.remote.model.MovieDetailDto
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.MovieDetail

private const val POSTER_BASE_URL_W500 = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL_W1280 = "https://image.tmdb.org/t/p/w1280"

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
)

private fun GenreDto.toDomain() = Genre(id = id, name = name)

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
    cachedAt = System.currentTimeMillis(),
)
