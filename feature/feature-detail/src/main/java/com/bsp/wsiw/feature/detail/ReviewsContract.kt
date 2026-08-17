package com.bsp.wsiw.feature.detail

import androidx.annotation.StringRes
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.ui.UiText

enum class ReviewSort(@param:StringRes val labelRes: Int) {
    Newest(R.string.reviews_sort_newest),
    Oldest(R.string.reviews_sort_oldest),
    HighestRated(R.string.reviews_sort_highest_rated),
    LowestRated(R.string.reviews_sort_lowest_rated),
}

sealed interface ReviewsAction {
    data class SelectSort(val sort: ReviewSort) : ReviewsAction
    data object LoadNextPage : ReviewsAction
    data object Retry : ReviewsAction
}

sealed interface ReviewsEvent {
    data class ShowSnackbar(val message: UiText) : ReviewsEvent
}

data class ReviewsUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val sort: ReviewSort = ReviewSort.Newest,
    val error: UiText? = null,
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
