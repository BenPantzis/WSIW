package com.bsp.wsiw.feature.profile

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.repository.AuthRepository
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TMDB_APPROVE_BASE = "https://www.themoviedb.org/auth/access"
private const val CALLBACK_URL = "wsiw://auth/callback"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel<ProfileAction, ProfileEvent, ProfileUiState>(
    initialState = ProfileUiState(),
) {
    init {
        viewModelScope.launch {
            combine(
                sessionRepository.isAuthenticated,
                sessionRepository.accountName,
                sessionRepository.avatarUrl,
            ) { isAuth, name, avatar ->
                Triple(isAuth, name, avatar)
            }.collect { (isAuth, name, avatar) ->
                updateState {
                    copy(
                        isAuthenticated = isAuth,
                        accountName = name,
                        avatarUrl = avatar,
                        isSigningIn = false,
                        error = null,
                    )
                }
            }
        }
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.SignIn -> startSignIn()
            ProfileAction.SignOut -> signOut()
        }
    }

    private fun startSignIn() {
        updateState { copy(isSigningIn = true, error = null) }
        viewModelScope.launch {
            try {
                val requestToken = authRepository.getRequestToken()
                val url = "$TMDB_APPROVE_BASE?request_token=$requestToken&redirect_to=$CALLBACK_URL"
                sendEvent(ProfileEvent.OpenBrowser(url))
            } catch (_: Exception) {
                updateState { copy(isSigningIn = false, error = "Couldn't reach TMDB. Check your connection.") }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (_: Exception) { }
        }
    }
}
