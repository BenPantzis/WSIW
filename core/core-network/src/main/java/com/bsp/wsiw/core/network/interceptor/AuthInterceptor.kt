package com.bsp.wsiw.core.network.interceptor

import com.bsp.wsiw.core.common.auth.TokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.getAccessToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        // On 401, wipe stored tokens so the app can redirect to login.
        // To add refresh: call your token-refresh endpoint here, update via
        // tokenProvider.setTokens(...), close this response, and retry once.
        if (response.code == 401) {
            runBlocking { tokenProvider.clearTokens() }
        }

        return response
    }
}
