package com.bsp.wsiw.feature.watchlist

import app.cash.turbine.test
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getWatchlist: GetWatchlistUseCase = mockk()
    private val toggleWatchlist: ToggleWatchlistUseCase = mockk()

    private fun createViewModel() = WatchlistViewModel(getWatchlist, toggleWatchlist)

    @Test
    fun `watchlist flow populates movies and clears loading`() {
        val movies = listOf(fakeMovie(id = 1), fakeMovie(id = 2))
        every { getWatchlist() } returns flowOf(movies)

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(movies, vm.uiState.value.movies)
    }

    @Test
    fun `empty watchlist clears loading with empty list`() {
        every { getWatchlist() } returns flowOf(emptyList())

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.movies.isEmpty())
    }

    @Test
    fun `removeMovie calls toggleWatchlist with isWatchlisted = true`() = runTest {
        val movie = fakeMovie(id = 42)
        every { getWatchlist() } returns flowOf(listOf(movie))
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = createViewModel()
        vm.onAction(WatchlistAction.RemoveMovie(movieId = 42))

        coVerify { toggleWatchlist(movie, isWatchlisted = true) }
    }

    @Test
    fun `removeMovie sends snackbar event`() = runTest {
        val movie = fakeMovie(id = 7)
        every { getWatchlist() } returns flowOf(listOf(movie))
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = createViewModel()

        vm.events.test {
            vm.onAction(WatchlistAction.RemoveMovie(movieId = 7))
            assertTrue(awaitItem() is WatchlistEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeMovie with unknown id does nothing`() = runTest {
        val movie = fakeMovie(id = 1)
        every { getWatchlist() } returns flowOf(listOf(movie))

        val vm = createViewModel()
        vm.onAction(WatchlistAction.RemoveMovie(movieId = 999))

        coVerify(exactly = 0) { toggleWatchlist(any(), any()) }
    }
}
