package com.bsp.wsiw.core.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class WatchProvider(val name: String, val logoUrl: String)

@Stable
data class WatchProviders(
    val streaming: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList(),
) {
    val isEmpty: Boolean get() = streaming.isEmpty() && rent.isEmpty() && buy.isEmpty()
}
