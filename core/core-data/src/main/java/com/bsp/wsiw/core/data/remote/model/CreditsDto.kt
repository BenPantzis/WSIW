package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class CastMemberDto(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path") val profilePath: String?,
    val order: Int,
)

data class CreditsDto(val cast: List<CastMemberDto> = emptyList())
