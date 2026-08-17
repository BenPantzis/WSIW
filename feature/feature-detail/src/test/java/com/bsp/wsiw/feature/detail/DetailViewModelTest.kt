package com.bsp.wsiw.feature.detail

import android.content.Context
import app.cash.turbine.test
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.usecase.GetMovieDetailUseCase
import com.bsp.wsiw.core.domain.usecase.GetMovieRatingUseCase
import com.bsp.wsiw.core.domain.usecase.GetMovieReviewsUseCase
import com.bsp.wsiw.core.domain.usecase.GetWatchProvidersUseCase
import com.bsp.wsiw.core.domain.usecase.IsWatchlistedUseCase
import com.bsp.wsiw.core.domain.usecase.RateMovieUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovieDetail
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

class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieDetail: GetMovieDetailUseCase = mockk()
    private val isWatchlisted: IsWatchlistedUseCase = mockk()
    private val toggleWatchlist: ToggleWatchlistUseCase = mockk()
    private val getMovieRating: GetMovieRatingUseCase = mockk()
    private val rateMovie: RateMovieUseCase = mockk()
    private val getWatchProviders: GetWatchProvidersUseCase = mockk()
    private val getMovieReviews: GetMovieReviewsUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    // Context only touched by extractPalette, which short-circuits when posterUrl == null
    private val context: Context = mockk()

    @Before
    fun setup() {
        every { getMovieDetail(any()) } returns flowOf(Result.Success(fakeMovieDetail()))
        every { isWatchlisted(any()) } returns flowOf(false)
        every { getMovieRating(any()) } returns flowOf(null)
        every { sessionRepository.isAuthenticated } returns flowOf(false)
        every { getWatchProviders(any()) } returns flowOf(Result.Loading)
        every { getMovieReviews(any()) } returns flowOf(Result.Loading)
    }

    private fun createViewModel(movieId: Int = 1) = DetailViewModel(
        movieId = movieId,
        getMovieDetail = getMovieDetail,
        isWatchlisted = isWatchlisted,
        toggleWatchlist = toggleWatchlist,
        getMovieRating = getMovieRating,
        rateMovie = rateMovie,
        getWatchProviders = getWatchProviders,
        getMovieReviews = getMovieReviews,
        sessionRepository = sessionRepository,
        context = context,
    )

    // --- Load ---

    @Test
    fun `load success sets movie and clears loading`() {
        val detail = fakeMovieDetail()
        every { getMovieDetail(any()) } returns flowOf(Result.Success(detail))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(detail, vm.uiState.value.movie)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `load error without cached movie sets error state`() {
        every { getMovieDetail(any()) } returns flowOf(Result.Error(RuntimeException("Not found")))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.movie)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `load error with null exception message still sets error state`() {
        every { getMovieDetail(any()) } returns flowOf(Result.Error(null))

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `load error with cached movie sends ShowError event and preserves movie`() = runTest {
        val detail = fakeMovieDetail()
        every { getMovieDetail(any()) } returnsMany listOf(
            flowOf(Result.Success(detail)),
            flowOf(Result.Error(RuntimeException("Refresh failed"))),
        )

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.movie)

        vm.events.test {
            vm.onAction(DetailAction.Retry)
            assertTrue(awaitItem() is DetailEvent.ShowError)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(detail, vm.uiState.value.movie)
    }

    @Test
    fun `retry triggers a second load`() {
        val vm = createViewModel()

        vm.onAction(DetailAction.Retry)

        coVerify(exactly = 2) { getMovieDetail(any()) }
    }

    // --- Watchlist ---

    @Test
    fun `isWatchlisted flow updates watchlist state`() {
        every { isWatchlisted(any()) } returns flowOf(true)

        val vm = createViewModel()

        assertTrue(vm.uiState.value.isWatchlisted)
    }

    @Test
    fun `toggleWatchlist when authenticated and not watchlisted passes false`() = runTest {
        every { sessionRepository.isAuthenticated } returns flowOf(true)
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = createViewModel()
        vm.onAction(DetailAction.ToggleWatchlist)

        coVerify { toggleWatchlist(any(), isWatchlisted = false) }
    }

    @Test
    fun `toggleWatchlist when authenticated and watchlisted passes true`() = runTest {
        every { isWatchlisted(any()) } returns flowOf(true)
        every { sessionRepository.isAuthenticated } returns flowOf(true)
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = createViewModel()
        vm.onAction(DetailAction.ToggleWatchlist)

        coVerify { toggleWatchlist(any(), isWatchlisted = true) }
    }

    @Test
    fun `toggleWatchlist when unauthenticated fires SignInRequired`() = runTest {
        val vm = createViewModel()

        vm.events.test {
            vm.onAction(DetailAction.ToggleWatchlist)
            assertTrue(awaitItem() is DetailEvent.SignInRequired)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Ratings ---

    @Test
    fun `userRating flow updates state`() {
        every { getMovieRating(any()) } returns flowOf(7f)

        val vm = createViewModel()

        assertEquals(7f, vm.uiState.value.userRating)
    }

    @Test
    fun `showRatingDialog when unauthenticated fires SignInRequired`() = runTest {
        val vm = createViewModel()

        vm.events.test {
            vm.onAction(DetailAction.ShowRatingDialog)
            assertTrue(awaitItem() is DetailEvent.SignInRequired)
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(vm.uiState.value.showRatingDialog)
    }

    @Test
    fun `showRatingDialog when authenticated sets showRatingDialog = true`() {
        every { sessionRepository.isAuthenticated } returns flowOf(true)

        val vm = createViewModel()
        vm.onAction(DetailAction.ShowRatingDialog)

        assertTrue(vm.uiState.value.showRatingDialog)
    }

    @Test
    fun `dismissRatingDialog clears showRatingDialog`() {
        every { sessionRepository.isAuthenticated } returns flowOf(true)

        val vm = createViewModel()
        vm.onAction(DetailAction.ShowRatingDialog)
        vm.onAction(DetailAction.DismissRatingDialog)

        assertFalse(vm.uiState.value.showRatingDialog)
    }

    @Test
    fun `rateMovie fires RatingSubmitted event with correct rating`() = runTest {
        coEvery { rateMovie(any(), any()) } just Runs

        val vm = createViewModel()

        vm.events.test {
            vm.onAction(DetailAction.RateMovie(rating = 8f))
            val event = awaitItem()
            assertTrue(event is DetailEvent.RatingSubmitted)
            assertEquals(8f, (event as DetailEvent.RatingSubmitted).rating)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rateMovie calls use case and dismisses dialog`() = runTest {
        coEvery { rateMovie(any(), any()) } just Runs

        val vm = createViewModel()
        vm.onAction(DetailAction.RateMovie(rating = 6f))

        coVerify { rateMovie(any(), 6f) }
        assertFalse(vm.uiState.value.showRatingDialog)
    }

    @Test
    fun `removeRating calls rateMovie remove and dismisses dialog`() = runTest {
        coEvery { rateMovie.remove(any()) } just Runs

        val vm = createViewModel()
        vm.onAction(DetailAction.RemoveRating)

        coVerify { rateMovie.remove(any()) }
        assertFalse(vm.uiState.value.showRatingDialog)
    }
}
