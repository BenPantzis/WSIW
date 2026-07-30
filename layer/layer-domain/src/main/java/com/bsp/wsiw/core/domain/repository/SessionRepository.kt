package com.bsp.wsiw.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val accountObjectId: Flow<String?>
    val accountName: Flow<String?>
    val avatarUrl: Flow<String?>
    val isAuthenticated: Flow<Boolean>
    suspend fun saveSession(accountObjectId: String, accountName: String, avatarUrl: String?)
    suspend fun clearSession()
}
