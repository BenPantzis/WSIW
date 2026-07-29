package com.bsp.wsiw.core.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val runtime: Int,
    val originalLanguage: String,
    val trailer: VideoEntry? = null,
    val cast: List<CastMember> = emptyList(),
    val similarMovies: List<Movie> = emptyList(),
)
