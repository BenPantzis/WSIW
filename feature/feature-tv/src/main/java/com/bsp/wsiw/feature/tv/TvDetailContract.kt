package com.bsp.wsiw.feature.tv

import com.bsp.wsiw.core.domain.model.TvShowDetail
import com.bsp.wsiw.core.ui.UiText

sealed interface TvDetailAction {
    data object Retry : TvDetailAction
}

sealed interface TvDetailEvent {
    data class ShowError(val message: UiText) : TvDetailEvent
}

data class TvDetailUiState(
    val isLoading: Boolean = true,
    val show: TvShowDetail? = null,
    val error: UiText? = null,
)
