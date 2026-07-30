package com.bsp.wsiw.feature.profile

sealed interface ProfileAction {
    data object SignIn : ProfileAction
    data object SignOut : ProfileAction
    data object CompleteSignIn : ProfileAction
    data object CancelSignIn : ProfileAction
}

sealed interface ProfileEvent {
    data class OpenBrowser(val url: String) : ProfileEvent
}

data class ProfileUiState(
    val isAuthenticated: Boolean = false,
    val accountName: String? = null,
    val avatarUrl: String? = null,
    val isSigningIn: Boolean = false,
    val pendingRequestToken: String? = null,
    val isExchangingToken: Boolean = false,
    val error: String? = null,
) {
    val isAwaitingApproval get() = pendingRequestToken != null && !isSigningIn
}

sealed interface LoginCallbackAction {
    data object Retry : LoginCallbackAction
}

sealed interface LoginCallbackEvent {
    data object NavigateToProfile : LoginCallbackEvent
}

data class LoginCallbackUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
)
