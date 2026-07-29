package com.bsp.wsiw.feature.detail

import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.ui.UiText

sealed interface DetailAction {
    data object Retry : DetailAction
    data object ToggleWatchlist : DetailAction
}

sealed interface DetailEvent {
    data class ShowError(val message: UiText) : DetailEvent
}

data class DetailUiState(
    val isLoading: Boolean = true,
    val movie: MovieDetail? = null,
    val error: UiText? = null,
    val accentArgb: Int? = null,
    val isWatchlisted: Boolean = false,
    val isRefreshing: Boolean = false,
    val previewReviews: List<Review> = emptyList(),
    val totalReviewCount: Int = 0,
)
