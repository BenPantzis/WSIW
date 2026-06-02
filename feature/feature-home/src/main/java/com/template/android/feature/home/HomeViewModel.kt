package com.template.android.feature.home

import androidx.lifecycle.viewModelScope
import com.template.android.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<HomeAction, HomeEvent, HomeUiState>(
    initialState = HomeUiState(),
) {
    init {
        onAction(HomeAction.LoadContent)
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            HomeAction.LoadContent -> loadContent()
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            Timber.d("loadContent")
            updateState { copy(isLoading = true) }
            updateState { copy(isLoading = false, message = "Hello, World!") }
        }
    }
}

sealed interface HomeAction {
    data object LoadContent : HomeAction
}

sealed interface HomeEvent {
    data class ShowSnackbar(val message: String) : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val message: String = "",
)
