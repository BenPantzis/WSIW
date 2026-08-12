package com.bsp.wsiw.core.domain.model

data class TvShow(
    val id: Int,
    val name: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val firstAirDate: String?,
    val voteAverage: Double,
    val genreIds: List<Int>,
)
