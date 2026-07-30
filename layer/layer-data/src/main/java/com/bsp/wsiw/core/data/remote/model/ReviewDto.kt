package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class ReviewListResponseDto(
    val results: List<ReviewDto> = emptyList(),
    @SerializedName("total_pages") val totalPages: Int = 1,
)

data class ReviewDto(
    val id: String,
    val author: String,
    @SerializedName("author_details") val authorDetails: ReviewAuthorDetailsDto,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
)

data class ReviewAuthorDetailsDto(
    @SerializedName("avatar_path") val avatarPath: String?,
    val rating: Float?,
)
