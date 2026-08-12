package com.bsp.wsiw.core.domain.model

data class TvShowDetail(
    val id: Int,
    val name: String,
    val overview: String,
    val tagline: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val firstAirDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val status: String,
    val genres: List<Genre>,
    val seasons: List<Season>,
    val networks: List<String>,
    val contentRating: String?,
    val cast: List<CastMember>,
    val similar: List<TvShow>,
    val recommendations: List<TvShow>,
    val trailerKey: String?,
)
