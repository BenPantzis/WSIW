package com.bsp.wsiw.auth

import com.bsp.wsiw.BuildConfig
import com.bsp.wsiw.core.common.auth.TokenProvider
import javax.inject.Inject

class TmdbTokenProvider @Inject constructor() : TokenProvider {
    override suspend fun getAccessToken(): String = BuildConfig.TMDB_ACCESS_TOKEN
    override suspend fun setTokens(accessToken: String, refreshToken: String) = Unit
    override suspend fun clearTokens() = Unit
}
