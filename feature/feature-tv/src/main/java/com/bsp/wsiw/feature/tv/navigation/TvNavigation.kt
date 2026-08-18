@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.tv.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.tv.TvDetailScreen
import com.bsp.wsiw.feature.tv.TvListScreen
import kotlinx.serialization.Serializable

@Serializable
data object TvKey : NavKey

@Serializable
data class TvDetailKey(val seriesId: Int) : NavKey

fun EntryProviderScope<NavKey>.tvListDestination(
    onShowClick: (Int) -> Unit,
) {
    entry<TvKey> { TvListScreen(onShowClick = onShowClick) }
}

fun EntryProviderScope<NavKey>.tvDetailDestination(
    metadata: Map<String, Any> = emptyMap(),
    onBack: () -> Unit,
    onShowClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
) {
    entry<TvDetailKey>(metadata = metadata) { key ->
        TvDetailScreen(
            seriesId = key.seriesId,
            onBack = onBack,
            onShowClick = onShowClick,
            onPersonClick = onPersonClick,
        )
    }
}
