package com.bsp.wsiw.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface WatchlistPreferences {
    val isListView: Flow<Boolean>
    suspend fun setListView(isList: Boolean)
}
