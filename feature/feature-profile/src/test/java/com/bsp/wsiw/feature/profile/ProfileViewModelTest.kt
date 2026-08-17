package com.bsp.wsiw.feature.profile

import app.cash.turbine.test
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.usecase.CreateUserSessionUseCase
import com.bsp.wsiw.core.domain.usecase.GetAllRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.GetLocalRatedMoviesUseCase
import com.bsp.wsiw.core.domain.usecase.GetRequestTokenUseCase
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.SignOutUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.ui.UiText
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository: SessionRepository = mockk()
    private val getWatchlist: GetWatchlistUseCase = mockk()
    private val getAllRatings: GetAllRatingsUseCase = mockk()
    private val getLocalRatedMovies: GetLocalRatedMoviesUseCase = mockk()
    private val getRequestToken: GetRequestTokenUseCase = mockk()
    private val createUserSession: CreateUserSessionUseCase = mockk()
    private val signOut: SignOutUseCase = mockk()

    @Before
    fun setup() {
        every { sessionRepository.isAuthenticated } returns flowOf(false)
        every { sessionRepository.accountName } returns flowOf(null)
        every { sessionRepository.avatarUrl } returns flowOf(null)
        every { getWatchlist() } returns flowOf(emptyList())
        every { getAllRatings() } returns flowOf(emptyMap())
        every { getLocalRatedMovies() } returns flowOf(emptyList())
        coEvery { getRequestToken() } returns "test_token"
        coEvery { createUserSession(any()) } just Runs
        coEvery { signOut() } just Runs
    }

    private fun createViewModel() = ProfileViewModel(
        sessionRepository = sessionRepository,
        getWatchlist = getWatchlist,
        getAllRatings = getAllRatings,
        getLocalRatedMovies = getLocalRatedMovies,
        getRequestToken = getRequestToken,
        createUserSession = createUserSession,
        signOut = signOut,
    )

    // --- Session state ---

    @Test
    fun `initial unauthenticated state reflects session repository`() {
        val vm = createViewModel()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertNull(vm.uiState.value.accountName)
    }

    @Test
    fun `authenticated state updates when session is active`() {
        every { sessionRepository.isAuthenticated } returns flowOf(true)
        every { sessionRepository.accountName } returns flowOf("JohnDoe")

        val vm = createViewModel()

        assertTrue(vm.uiState.value.isAuthenticated)
        assertEquals("JohnDoe", vm.uiState.value.accountName)
    }

    @Test
    fun `watchlist count reflects use case result`() {
        every { getWatchlist() } returns flowOf(listOf(mockk(), mockk(), mockk()))

        val vm = createViewModel()

        assertEquals(3, vm.uiState.value.watchlistCount)
    }

    @Test
    fun `ratings count and average reflect use case result`() {
        every { getAllRatings() } returns flowOf(mapOf(1 to 8f, 2 to 6f))

        val vm = createViewModel()

        assertEquals(2, vm.uiState.value.ratingsCount)
        assertEquals(7f, vm.uiState.value.averageRating)
    }

    // --- Sign in ---

    @Test
    fun `startSignIn sends OpenBrowser event with correct token URL`() = runTest {
        coEvery { getRequestToken() } returns "abc123"

        val vm = createViewModel()

        vm.events.test {
            vm.onAction(ProfileAction.SignIn)
            val event = awaitItem() as ProfileEvent.OpenBrowser
            assertTrue(event.url.contains("abc123"))
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("abc123", vm.uiState.value.pendingRequestToken)
        assertFalse(vm.uiState.value.isSigningIn)
    }

    @Test
    fun `startSignIn failure sets error state`() = runTest {
        coEvery { getRequestToken() } throws RuntimeException("Network error")

        val vm = createViewModel()
        vm.onAction(ProfileAction.SignIn)

        assertFalse(vm.uiState.value.isSigningIn)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `completeSignIn on success clears pending token`() = runTest {
        coEvery { getRequestToken() } returns "abc123"
        coEvery { createUserSession(any()) } just Runs

        val vm = createViewModel()
        vm.onAction(ProfileAction.SignIn)
        assertEquals("abc123", vm.uiState.value.pendingRequestToken)

        vm.onAction(ProfileAction.CompleteSignIn)

        assertNull(vm.uiState.value.pendingRequestToken)
        assertFalse(vm.uiState.value.isExchangingToken)
    }

    @Test
    fun `completeSignIn failure sets error state`() = runTest {
        coEvery { getRequestToken() } returns "abc123"
        coEvery { createUserSession(any()) } throws RuntimeException("Exchange failed")

        val vm = createViewModel()
        vm.onAction(ProfileAction.SignIn)
        vm.onAction(ProfileAction.CompleteSignIn)

        assertFalse(vm.uiState.value.isExchangingToken)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `cancelSignIn clears pending token`() = runTest {
        coEvery { getRequestToken() } returns "abc123"

        val vm = createViewModel()
        vm.onAction(ProfileAction.SignIn)
        assertNotNull(vm.uiState.value.pendingRequestToken)

        vm.onAction(ProfileAction.CancelSignIn)

        assertNull(vm.uiState.value.pendingRequestToken)
        assertFalse(vm.uiState.value.isExchangingToken)
    }

    // --- Sign out ---

    @Test
    fun `signOut calls use case`() = runTest {
        val vm = createViewModel()
        vm.onAction(ProfileAction.SignOut)

        coVerify { signOut() }
    }
}
