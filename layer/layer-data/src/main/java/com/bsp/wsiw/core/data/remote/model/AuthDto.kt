package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class RequestTokenDto(
    @SerializedName("request_token") val requestToken: String = "",
    val success: Boolean = false,
)

data class AccessTokenBody(
    @SerializedName("request_token") val requestToken: String,
)

data class AccessTokenDto(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("account_object_id") val accountObjectId: String = "",
    val success: Boolean = false,
)

data class DeleteTokenDto(val success: Boolean = false)

data class AccountDto(
    val id: Int = 0,
    val username: String = "",
    val name: String = "",
    val avatar: AvatarDto? = null,
)

data class AvatarDto(
    val tmdb: TmdbAvatarDto? = null,
)

data class TmdbAvatarDto(
    @SerializedName("avatar_path") val avatarPath: String? = null,
)
