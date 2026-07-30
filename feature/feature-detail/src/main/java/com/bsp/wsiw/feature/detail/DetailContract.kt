package com.bsp.wsiw.feature.detail

import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.domain.model.WatchProviders
import com.bsp.wsiw.core.ui.UiText

sealed interface DetailAction {
    data object Retry : DetailAction
    data object ToggleWatchlist : DetailAction
    data object ShowRatingDialog : DetailAction
    data object DismissRatingDialog : DetailAction
    data class RateMovie(val stars: Int) : DetailAction
    data object RemoveRating : DetailAction
}

sealed interface DetailEvent {
    data class ShowError(val message: UiText) : DetailEvent
    data object SignInRequired : DetailEvent
}

data class DetailUiState(
    val isLoading: Boolean = true,
    val movie: MovieDetail? = null,
    val error: UiText? = null,
    val accentArgb: Int? = null,
    val isWatchlisted: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isRefreshing: Boolean = false,
    val previewReviews: List<Review> = emptyList(),
    val totalReviewCount: Int = 0,
    val watchProviders: WatchProviders? = null,
    val userRating: Float? = null,
    val showRatingDialog: Boolean = false,
)
