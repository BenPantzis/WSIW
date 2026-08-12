package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class MultiSearchResponseDto(
    val results: List<MultiSearchItemDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int,
)

data class MultiSearchItemDto(
    val id: Int,
    @SerializedName("media_type") val mediaType: String,
    // movie fields
    val title: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    // tv fields
    val name: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    // person fields
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("known_for_department") val knownForDepartment: String?,
)
