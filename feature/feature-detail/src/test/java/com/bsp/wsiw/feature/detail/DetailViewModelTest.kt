package com.bsp.wsiw.feature.detail

import android.content.Context
import app.cash.turbine.test
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.usecase.GetMovieDetailUseCase
import com.bsp.wsiw.core.domain.usecase.IsWatchlistedUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import com.bsp.wsiw.core.testing.fakeMovieDetail
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
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieDetail: GetMovieDetailUseCase = mockk()
    private val isWatchlisted: IsWatchlistedUseCase = mockk()
    private val toggleWatchlist: ToggleWatchlistUseCase = mockk()
    // Context is only used in extractPalette, which returns early when posterUrl == null
    private val context: Context = mockk()

    private fun createViewModel(movieId: Int = 1) = DetailViewModel(
        movieId = movieId,
        getMovieDetail = getMovieDetail,
        isWatchlisted = isWatchlisted,
        toggleWatchlist = toggleWatchlist,
        context = context,
    )

    @Test
    fun `load success sets movie and clears loading`() {
        val detail = fakeMovieDetail()
        every { getMovieDetail(any()) } returns flowOf(Result.Success(detail))
        every { isWatchlisted(any()) } returns flowOf(false)

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(detail, vm.uiState.value.movie)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `load error without cached movie sets error string`() {
        every { getMovieDetail(any()) } returns flowOf(
            Result.Error(RuntimeException("Not found"))
        )
        every { isWatchlisted(any()) } returns flowOf(false)

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.movie)
        assertEquals("Not found", vm.uiState.value.error)
    }

    @Test
    fun `load error with null message uses fallback`() {
        every { getMovieDetail(any()) } returns flowOf(Result.Error(null))
        every { isWatchlisted(any()) } returns flowOf(false)

        val vm = createViewModel()

        assertEquals("Something went wrong", vm.uiState.value.error)
    }

    @Test
    fun `load error with cached movie sends event and preserves movie`() = runTest {
        val detail = fakeMovieDetail()
        var callCount = 0
        every { getMovieDetail(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(detail))
            else flowOf(Result.Error(RuntimeException("Refresh failed")))
        }
        every { isWatchlisted(any()) } returns flowOf(false)

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
    fun `isWatchlisted flow updates watchlist state`() {
        every { getMovieDetail(any()) } returns flowOf(Result.Success(fakeMovieDetail()))
        every { isWatchlisted(any()) } returns flowOf(true)

        val vm = createViewModel()

        assertTrue(vm.uiState.value.isWatchlisted)
    }

    @Test
    fun `toggleWatchlist when not watchlisted passes isWatchlisted = false`() = runTest {
        val detail = fakeMovieDetail(id = 5)
        every { getMovieDetail(any()) } returns flowOf(Result.Success(detail))
        every { isWatchlisted(any()) } returns flowOf(false)
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = createViewModel(movieId = 5)
        vm.onAction(DetailAction.ToggleWatchlist)

        coVerify { toggleWatchlist(any(), isWatchlisted = false) }
    }

    @Test
    fun `toggleWatchlist when watchlisted passes isWatchlisted = true`() = runTest {
        val detail = fakeMovieDetail(id = 5)
        every { getMovieDetail(any()) } returns flowOf(Result.Success(detail))
        every { isWatchlisted(any()) } returns flowOf(true)
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = createViewModel(movieId = 5)
        vm.onAction(DetailAction.ToggleWatchlist)

        coVerify { toggleWatchlist(any(), isWatchlisted = true) }
    }

    @Test
    fun `retry triggers a second load`() {
        val detail = fakeMovieDetail()
        every { getMovieDetail(any()) } returns flowOf(Result.Success(detail))
        every { isWatchlisted(any()) } returns flowOf(false)

        val vm = createViewModel()
        vm.onAction(DetailAction.Retry)

        coVerify(exactly = 2) { getMovieDetail(any()) }
    }
}
