package com.bsp.wsiw.feature.profile

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.usecase.CreateUserSessionUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = LoginCallbackViewModel.Factory::class)
class LoginCallbackViewModel @AssistedInject constructor(
    @Assisted val requestToken: String,
    private val createUserSession: CreateUserSessionUseCase,
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
                createUserSession(requestToken)
                sendEvent(LoginCallbackEvent.NavigateToProfile)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                updateState { copy(isLoading = false, error = UiText.StringResource(R.string.login_callback_error_generic)) }
            }
        }
    }
}
