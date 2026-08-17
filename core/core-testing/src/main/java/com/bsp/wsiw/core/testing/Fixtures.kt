package com.bsp.wsiw.core.testing

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.model.TvShowDetail

fun fakeMovie(
    id: Int = 1,
    title: String = "Test Movie",
    posterUrl: String? = null,
    backdropUrl: String? = null,
) = Movie(
    id = id,
    title = title,
    overview = "A test overview.",
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = "2024-01-01",
    voteAverage = 7.5,
    voteCount = 1000,
)

fun fakeMovieDetail(
    id: Int = 1,
    title: String = "Test Movie",
    posterUrl: String? = null,
    backdropUrl: String? = null,
) = MovieDetail(
    id = id,
    title = title,
    tagline = "A test tagline.",
    overview = "A test overview.",
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = "January 1, 2024",
    voteAverage = 7.5,
    voteCount = 1000,
    genres = emptyList(),
    runtime = 120,
    originalLanguage = "en",
)

fun fakeTvShow(
    id: Int = 1,
    name: String = "Test Show",
) = TvShow(
    id = id,
    name = name,
    overview = "A test overview.",
    posterUrl = null,
    backdropUrl = null,
    firstAirDate = "2024-01-01",
    voteAverage = 8.0,
    genreIds = emptyList(),
)

fun fakeTvShowDetail(
    id: Int = 1,
    name: String = "Test Show",
) = TvShowDetail(
    id = id,
    name = name,
    overview = "A test overview.",
    tagline = "A test tagline.",
    posterUrl = null,
    backdropUrl = null,
    firstAirDate = "2024-01-01",
    voteAverage = 8.0,
    voteCount = 500,
    status = "Returning Series",
    genres = emptyList(),
    seasons = emptyList(),
    networks = emptyList(),
    contentRating = null,
    cast = emptyList(),
    similar = emptyList(),
    recommendations = emptyList(),
    trailerKey = null,
)
