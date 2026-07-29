package com.bsp.wsiw.feature.detail

import com.bsp.wsiw.core.domain.model.Review

enum class ReviewSort(val label: String) {
    Newest("Newest"),
    Oldest("Oldest"),
    HighestRated("Highest Rated"),
    LowestRated("Lowest Rated"),
}

sealed interface ReviewsAction {
    data class SelectSort(val sort: ReviewSort) : ReviewsAction
    data object LoadNextPage : ReviewsAction
    data object Retry : ReviewsAction
}

data class ReviewsUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val sort: ReviewSort = ReviewSort.Newest,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = Int.MAX_VALUE,
) {
    val canLoadMore get() = !isLoading && !isLoadingMore && currentPage < totalPages
    val sorted get() = when (sort) {
        ReviewSort.Newest -> reviews
        ReviewSort.Oldest -> reviews.reversed()
        ReviewSort.HighestRated -> reviews.sortedByDescending { it.rating ?: -1f }
        ReviewSort.LowestRated -> reviews.sortedBy { it.rating ?: Float.MAX_VALUE }
    }
}
