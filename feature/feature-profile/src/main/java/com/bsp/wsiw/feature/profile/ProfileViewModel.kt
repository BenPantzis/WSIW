package com.bsp.wsiw.feature.profile

import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.domain.repository.AuthRepository
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.usecase.GetAllRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.GetFavoriteCountUseCase
import com.bsp.wsiw.core.domain.usecase.GetLocalRatedMoviesUseCase
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TMDB_APPROVE_BASE = "https://www.themoviedb.org/auth/access"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val getWatchlist: GetWatchlistUseCase,
    private val getAllRatings: GetAllRatingsUseCase,
    private val getLocalRatedMovies: GetLocalRatedMoviesUseCase,
    private val getFavoriteCount: GetFavoriteCountUseCase,
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

        viewModelScope.launch {
            sessionRepository.accountId.collect { accountId ->
                if (accountId != null) loadFavoriteCount(accountId)
            }
        }
    }

    private fun loadFavoriteCount(accountId: Int) {
        viewModelScope.launch {
            try {
                val count = getFavoriteCount(accountId)
                updateState { copy(favoriteCount = count) }
            } catch (_: Exception) { }
        }
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.SignIn -> startSignIn()
            ProfileAction.SignOut -> signOut()
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
                val requestToken = authRepository.getRequestToken()
                val url = "$TMDB_APPROVE_BASE?request_token=$requestToken"
                updateState { copy(isSigningIn = false, pendingRequestToken = requestToken) }
                sendEvent(ProfileEvent.OpenBrowser(url))
            } catch (_: Exception) {
                updateState { copy(isSigningIn = false, error = "Couldn't reach TMDB. Check your connection.") }
            }
        }
    }

    private fun completeSignIn() {
        val token = uiState.value.pendingRequestToken ?: return
        updateState { copy(isExchangingToken = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.createUserSession(token)
                updateState { copy(pendingRequestToken = null, isExchangingToken = false) }
            } catch (_: Exception) {
                updateState {
                    copy(isExchangingToken = false, error = "Sign-in failed. Make sure you approved the request on TMDB.")
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            try { authRepository.signOut() } catch (_: Exception) { }
        }
    }
}
