package com.bsp.wsiw.feature.detail

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.usecase.GetMovieReviewsUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.R as CoreUiR
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ReviewsViewModel.Factory::class)
class ReviewsViewModel @AssistedInject constructor(
    @Assisted private val movieId: Int,
    private val getMovieReviews: GetMovieReviewsUseCase,
) : BaseViewModel<ReviewsAction, ReviewsEvent, ReviewsUiState>(
    initialState = ReviewsUiState(),
) {
    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): ReviewsViewModel
    }

    init {
        loadPage(page = 1, replace = true)
    }

    override fun handleAction(action: ReviewsAction) {
        when (action) {
            is ReviewsAction.SelectSort -> updateState { copy(sort = action.sort) }
            ReviewsAction.LoadNextPage -> {
                if (uiState.value.canLoadMore) loadPage(uiState.value.currentPage + 1, replace = false)
            }
            ReviewsAction.Retry -> loadPage(page = 1, replace = true)
        }
    }

    private fun loadPage(page: Int, replace: Boolean) {
        viewModelScope.launch {
            if (replace) updateState { copy(isLoading = reviews.isEmpty(), isLoadingMore = false, error = null) }
            else updateState { copy(isLoadingMore = true) }

            getMovieReviews(GetMovieReviewsUseCase.Params(movieId, page)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val paged = result.data
                        updateState {
                            copy(
                                isLoading = false,
                                isLoadingMore = false,
                                reviews = if (replace) paged.items else reviews + paged.items,
                                currentPage = page,
                                totalPages = paged.totalPages,
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        val hasReviews = uiState.value.reviews.isNotEmpty()
                        updateState {
                            copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = if (!hasReviews) UiText.StringResource(CoreUiR.string.error_something_went_wrong) else null,
                            )
                        }
                        if (hasReviews) sendEvent(ReviewsEvent.ShowSnackbar(UiText.StringResource(CoreUiR.string.error_something_went_wrong)))
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
