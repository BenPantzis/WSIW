package com.bsp.wsiw.core.data.tv

import com.bsp.wsiw.core.data.remote.model.TvShowDto
import com.bsp.wsiw.core.domain.model.TvShow

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"

internal fun TvShowDto.toDomain() = TvShow(
    id = id,
    name = name,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE + it },
    firstAirDate = firstAirDate,
    voteAverage = voteAverage,
    genreIds = genreIds,
)
