package com.bsp.wsiw.feature.profile

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.repository.AuthRepository
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = LoginCallbackViewModel.Factory::class)
class LoginCallbackViewModel @AssistedInject constructor(
    @Assisted val requestToken: String,
    private val authRepository: AuthRepository,
) : BaseViewModel<LoginCallbackAction, LoginCallbackEvent, LoginCallbackUiState>(
    initialState = LoginCallbackUiState(),
) {
    @AssistedFactory
    interface Factory {
        fun create(requestToken: String): LoginCallbackViewModel
    }

    init {
        exchangeToken()
    }

    override fun handleAction(action: LoginCallbackAction) {
        when (action) {
            LoginCallbackAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
                exchangeToken()
            }
        }
    }

    private fun exchangeToken() {
        viewModelScope.launch {
            try {
                authRepository.createUserSession(requestToken)
                sendEvent(LoginCallbackEvent.NavigateToProfile)
            } catch (_: Exception) {
                updateState { copy(isLoading = false, error = "Sign-in failed. Please try again.") }
            }
        }
    }
}
