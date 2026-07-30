package com.bsp.wsiw.core.data.auth

import com.bsp.wsiw.core.common.auth.TokenProvider
import com.bsp.wsiw.core.data.remote.TmdbAuthService
import com.bsp.wsiw.core.data.remote.model.AccessTokenBody
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.repository.AuthRepository
import javax.inject.Inject

private const val IMAGE_BASE = "https://image.tmdb.org/t/p/w185"

class AuthRepositoryImpl @Inject constructor(
    private val authService: TmdbAuthService,
    private val tokenProvider: TokenProvider,
    private val sessionRepository: SessionRepository,
) : AuthRepository {

    override suspend fun getRequestToken(): String =
        authService.getRequestToken().requestToken

    override suspend fun createUserSession(requestToken: String) {
        val tokenResponse = authService.getAccessToken(AccessTokenBody(requestToken))
        tokenProvider.saveAccessToken(tokenResponse.accessToken)
        val account = authService.getAccount()
        val displayName = account.name.ifBlank { account.username }
        val avatarPath = account.avatar?.tmdb?.avatarPath
        val avatarUrl = if (avatarPath != null) "$IMAGE_BASE$avatarPath" else null
        sessionRepository.saveSession(
            accountObjectId = tokenResponse.accountObjectId,
            accountName = displayName,
            avatarUrl = avatarUrl,
        )
    }

    override suspend fun signOut() {
        try {
            val currentToken = tokenProvider.getAccessToken()
            if (currentToken != null) {
                authService.deleteAccessToken(AccessTokenBody(currentToken))
            }
        } catch (_: Exception) { }
        tokenProvider.clearTokens()
        sessionRepository.clearSession()
    }
}
