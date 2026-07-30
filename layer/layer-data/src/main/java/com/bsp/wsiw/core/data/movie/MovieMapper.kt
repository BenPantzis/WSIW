package com.bsp.wsiw.core.data.movie

import com.bsp.wsiw.core.data.remote.model.MovieDto
import com.bsp.wsiw.core.database.entity.PopularMovieEntity
import com.bsp.wsiw.core.domain.model.Movie

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

fun MovieDto.toDomain() = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE_URL + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE_URL + it },
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
)

fun MovieDto.toEntity(page: Int) = PopularMovieEntity(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE_URL + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE_URL + it },
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    page = page,
    cachedAt = System.currentTimeMillis(),
)

fun PopularMovieEntity.toDomain() = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
)
