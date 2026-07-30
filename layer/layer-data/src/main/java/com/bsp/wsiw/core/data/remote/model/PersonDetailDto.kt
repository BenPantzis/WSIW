package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class PersonDetailDto(
    val id: Int,
    val name: String,
    val biography: String,
    val birthday: String?,
    @SerializedName("place_of_birth") val placeOfBirth: String?,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("known_for_department") val knownForDepartment: String,
    @SerializedName("movie_credits") val movieCredits: PersonMovieCreditsDto?,
)

data class PersonMovieCreditsDto(
    val cast: List<MovieDto> = emptyList(),
)
