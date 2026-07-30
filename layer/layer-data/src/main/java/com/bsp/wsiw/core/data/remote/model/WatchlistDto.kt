package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class WatchlistUpdateBody(
    @SerializedName("media_type") val mediaType: String = "movie",
    @SerializedName("media_id") val mediaId: Int,
    val watchlist: Boolean,
)

data class WatchlistUpdateResponseDto(
    val success: Boolean = false,
    @SerializedName("status_message") val statusMessage: String = "",
)
