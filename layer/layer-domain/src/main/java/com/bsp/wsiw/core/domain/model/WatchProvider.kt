package com.bsp.wsiw.core.domain.model

data class WatchProvider(val name: String, val logoUrl: String)

data class WatchProviders(
    val streaming: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList(),
) {
    val isEmpty: Boolean get() = streaming.isEmpty() && rent.isEmpty() && buy.isEmpty()
}
