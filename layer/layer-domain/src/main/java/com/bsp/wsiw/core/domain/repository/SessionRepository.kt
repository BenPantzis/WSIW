package com.bsp.wsiw.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val accountId: Flow<Int?>
    val accountObjectId: Flow<String?>
    val accountName: Flow<String?>
    val avatarUrl: Flow<String?>
    val isAuthenticated: Flow<Boolean>
    suspend fun saveSession(accountId: Int, accountObjectId: String, accountName: String, avatarUrl: String?)
    suspend fun clearSession()
}
