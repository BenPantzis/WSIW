package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class ReleaseDatesWrapperDto(
    val results: List<ReleaseDateCountryDto> = emptyList(),
)

data class ReleaseDateCountryDto(
    @SerializedName("iso_3166_1") val country: String,
    @SerializedName("release_dates") val releaseDates: List<ReleaseDateEntryDto> = emptyList(),
)

data class ReleaseDateEntryDto(
    val certification: String = "",
    val type: Int = 0,
)
