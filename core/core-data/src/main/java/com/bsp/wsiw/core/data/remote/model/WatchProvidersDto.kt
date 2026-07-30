package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class WatchProvidersResponseDto(
    val id: Int = 0,
    val results: Map<String, CountryWatchProvidersDto> = emptyMap(),
)

data class CountryWatchProvidersDto(
    val link: String? = null,
    val flatrate: List<WatchProviderDto> = emptyList(),
    val rent: List<WatchProviderDto> = emptyList(),
    val buy: List<WatchProviderDto> = emptyList(),
)

data class WatchProviderDto(
    @SerializedName("logo_path") val logoPath: String?,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("display_priority") val displayPriority: Int = 0,
)
