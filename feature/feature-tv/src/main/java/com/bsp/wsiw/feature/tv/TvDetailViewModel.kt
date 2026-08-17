package com.bsp.wsiw.feature.tv

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.usecase.GetTvDetailUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.R as CoreUiR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvDetailViewModel @Inject constructor(
    private val getTvDetail: GetTvDetailUseCase,
) : BaseViewModel<TvDetailAction, TvDetailEvent, TvDetailUiState>(
    initialState = TvDetailUiState(),
) {
    private var seriesId: Int = -1

    fun load(id: Int) {
        if (seriesId == id) return
        seriesId = id
        fetch()
    }

    override fun handleAction(action: TvDetailAction) {
        when (action) {
            TvDetailAction.Retry -> fetch()
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            getTvDetail(seriesId).collect { result ->
                when (result) {
                    is Result.Success -> updateState { copy(isLoading = false, show = result.data) }
                    is Result.Error -> updateState {
                        copy(isLoading = false, error = UiText.StringResource(CoreUiR.string.error_something_went_wrong))
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
