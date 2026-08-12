package com.bsp.wsiw.core.domain.model

data class Season(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val airDate: String?,
    val posterUrl: String?,
    val overview: String,
)
