package com.bsp.wsiw.core.datastore.auth

import androidx.datastore.preferences.core.stringPreferencesKey
import com.bsp.wsiw.core.common.auth.TokenProvider
import com.bsp.wsiw.core.datastore.PreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreTokenProvider @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : TokenProvider {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    override suspend fun getAccessToken(): String? =
        preferencesRepository.preferences.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()

    fun getRefreshToken() =
        preferencesRepository.preferences.map { it[REFRESH_TOKEN_KEY] }

    override suspend fun setTokens(accessToken: String, refreshToken: String) {
        preferencesRepository.put(ACCESS_TOKEN_KEY, accessToken)
        preferencesRepository.put(REFRESH_TOKEN_KEY, refreshToken)
    }

    override suspend fun clearTokens() {
        preferencesRepository.remove(ACCESS_TOKEN_KEY)
        preferencesRepository.remove(REFRESH_TOKEN_KEY)
    }
}
