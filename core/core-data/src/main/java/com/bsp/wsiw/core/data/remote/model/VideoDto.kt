package com.bsp.wsiw.core.data.remote.model

data class VideoDto(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean,
)

data class VideoResultsDto(val results: List<VideoDto> = emptyList())
