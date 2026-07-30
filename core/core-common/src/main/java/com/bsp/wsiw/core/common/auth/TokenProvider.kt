package com.bsp.wsiw.core.common.auth

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun saveAccessToken(token: String)
    suspend fun setTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
}
