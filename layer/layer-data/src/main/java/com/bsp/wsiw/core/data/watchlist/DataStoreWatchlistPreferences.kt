package com.bsp.wsiw.core.data.watchlist

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.bsp.wsiw.core.datastore.PreferencesRepository
import com.bsp.wsiw.core.domain.repository.WatchlistPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreWatchlistPreferences @Inject constructor(
    private val prefs: PreferencesRepository,
) : WatchlistPreferences {

    private val KEY = booleanPreferencesKey("watchlist_list_view")

    override val isListView: Flow<Boolean> = prefs.preferences.map { it[KEY] ?: false }

    override suspend fun setListView(isList: Boolean) = prefs.put(KEY, isList)
}
