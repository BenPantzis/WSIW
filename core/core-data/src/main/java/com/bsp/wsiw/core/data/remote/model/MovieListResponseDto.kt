package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class MovieListResponseDto(
    val page: Int,
    val results: List<MovieDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int,
)
