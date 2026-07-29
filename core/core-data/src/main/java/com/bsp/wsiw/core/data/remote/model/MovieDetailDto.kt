package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class MovieDetailDto(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    val genres: List<GenreDto>,
    val runtime: Int,
    @SerializedName("original_language") val originalLanguage: String,
    val videos: VideoResultsDto? = null,
    val credits: CreditsDto? = null,
    val similar: MovieListResponseDto? = null,
)

data class GenreDto(
    val id: Int,
    val name: String,
)
