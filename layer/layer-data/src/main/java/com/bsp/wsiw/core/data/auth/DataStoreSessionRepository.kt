package com.bsp.wsiw.core.data.auth

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bsp.wsiw.core.datastore.PreferencesRepository
import com.bsp.wsiw.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreSessionRepository @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : SessionRepository {

    companion object {
        private val KEY_ACCOUNT_ID = intPreferencesKey("tmdb_account_id")
        private val KEY_ACCOUNT_OBJECT_ID = stringPreferencesKey("tmdb_account_object_id")
        private val KEY_ACCOUNT_NAME = stringPreferencesKey("tmdb_account_name")
        private val KEY_AVATAR_URL = stringPreferencesKey("tmdb_avatar_url")
    }

    override val accountId: Flow<Int?> =
        preferencesRepository.preferences.map { it[KEY_ACCOUNT_ID] }

    override val accountObjectId: Flow<String?> =
        preferencesRepository.preferences.map { it[KEY_ACCOUNT_OBJECT_ID] }

    override val accountName: Flow<String?> =
        preferencesRepository.preferences.map { it[KEY_ACCOUNT_NAME] }

    override val avatarUrl: Flow<String?> =
        preferencesRepository.preferences.map { it[KEY_AVATAR_URL] }

    override val isAuthenticated: Flow<Boolean> =
        accountObjectId.map { it != null }

    override suspend fun saveSession(
        accountId: Int,
        accountObjectId: String,
        accountName: String,
        avatarUrl: String?,
    ) {
        preferencesRepository.put(KEY_ACCOUNT_ID, accountId)
        preferencesRepository.put(KEY_ACCOUNT_OBJECT_ID, accountObjectId)
        preferencesRepository.put(KEY_ACCOUNT_NAME, accountName)
        if (avatarUrl != null) {
            preferencesRepository.put(KEY_AVATAR_URL, avatarUrl)
        } else {
            preferencesRepository.remove(KEY_AVATAR_URL)
        }
    }

    override suspend fun clearSession() {
        preferencesRepository.remove(KEY_ACCOUNT_ID)
        preferencesRepository.remove(KEY_ACCOUNT_OBJECT_ID)
        preferencesRepository.remove(KEY_ACCOUNT_NAME)
        preferencesRepository.remove(KEY_AVATAR_URL)
    }
}
