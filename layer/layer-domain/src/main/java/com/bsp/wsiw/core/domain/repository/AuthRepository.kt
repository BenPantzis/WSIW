package com.bsp.wsiw.core.domain.repository

interface AuthRepository {
    suspend fun getRequestToken(): String
    suspend fun createUserSession(requestToken: String)
    suspend fun signOut()
}
