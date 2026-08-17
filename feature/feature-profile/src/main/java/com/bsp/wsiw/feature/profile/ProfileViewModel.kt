package com.bsp.wsiw.feature.profile

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.usecase.CreateUserSessionUseCase
import com.bsp.wsiw.core.domain.usecase.GetAllRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.GetLocalRatedMoviesUseCase
import com.bsp.wsiw.core.domain.usecase.GetRequestTokenUseCase
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.SignOutUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TMDB_APPROVE_BASE = "https://www.themoviedb.org/auth/access"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getWatchlist: GetWatchlistUseCase,
    private val getAllRatings: GetAllRatingsUseCase,
    private val getLocalRatedMovies: GetLocalRatedMoviesUseCase,
    private val getRequestToken: GetRequestTokenUseCase,
    private val createUserSession: CreateUserSessionUseCase,
    private val signOut: SignOutUseCase,
) : BaseViewModel<ProfileAction, ProfileEvent, ProfileUiState>(
    initialState = ProfileUiState(),
) {
    init {
        viewModelScope.launch {
            combine(
                sessionRepository.isAuthenticated,
                sessionRepository.accountName,
                sessionRepository.avatarUrl,
            ) { isAuth, name, avatar -> Triple(isAuth, name, avatar) }
                .collect { (isAuth, name, avatar) ->
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

        viewModelScope.launch {
            getWatchlist().collect { movies ->
                updateState { copy(watchlistCount = movies.size) }
            }
        }

        viewModelScope.launch {
            getAllRatings().collect { ratingsMap ->
                val avg = if (ratingsMap.isEmpty()) null
                          else ratingsMap.values.average().toFloat()
                updateState { copy(ratingsCount = ratingsMap.size, averageRating = avg) }
            }
        }

        viewModelScope.launch {
            getLocalRatedMovies().collect { movies ->
                updateState { copy(ratedMovies = movies) }
            }
        }
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.SignIn -> startSignIn()
            ProfileAction.SignOut -> doSignOut()
            ProfileAction.CompleteSignIn -> completeSignIn()
            ProfileAction.CancelSignIn -> updateState {
                copy(pendingRequestToken = null, isExchangingToken = false, error = null)
            }
        }
    }

    private fun startSignIn() {
        updateState { copy(isSigningIn = true, error = null) }
        viewModelScope.launch {
            try {
                val requestToken = getRequestToken()
                val url = "$TMDB_APPROVE_BASE?request_token=$requestToken"
                updateState { copy(isSigningIn = false, pendingRequestToken = requestToken) }
                sendEvent(ProfileEvent.OpenBrowser(url))
            } catch (_: Exception) {
                updateState { copy(isSigningIn = false, error = UiText.StringResource(R.string.profile_error_network)) }
            }
        }
    }

    private fun completeSignIn() {
        val token = uiState.value.pendingRequestToken ?: return
        updateState { copy(isExchangingToken = true, error = null) }
        viewModelScope.launch {
            try {
                createUserSession(token)
                updateState { copy(pendingRequestToken = null, isExchangingToken = false) }
            } catch (_: Exception) {
                updateState {
                    copy(isExchangingToken = false, error = UiText.StringResource(R.string.profile_error_sign_in_failed))
                }
            }
        }
    }

    private fun doSignOut() {
        viewModelScope.launch {
            try { signOut() } catch (_: Exception) { }
        }
    }
}
