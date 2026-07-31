package com.bsp.wsiw.feature.watchlist

import app.cash.turbine.test
import com.bsp.wsiw.core.domain.repository.WatchlistPreferences
import com.bsp.wsiw.core.domain.usecase.GetAllRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.GetWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.RefreshRatingsUseCase
import com.bsp.wsiw.core.domain.usecase.RefreshWatchlistUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getWatchlist: GetWatchlistUseCase = mockk()
    private val toggleWatchlist: ToggleWatchlistUseCase = mockk()
    private val refreshWatchlist: RefreshWatchlistUseCase = mockk()
    private val getAllRatings: GetAllRatingsUseCase = mockk()
    private val refreshRatings: RefreshRatingsUseCase = mockk()
    private val preferences: WatchlistPreferences = mockk()

    private fun createViewModel(): WatchlistViewModel {
        every { getWatchlist() } returns flowOf(emptyList())
        every { getAllRatings() } returns flowOf(emptyMap())
        every { preferences.isListView } returns flowOf(false)
        coEvery { refreshWatchlist() } just Runs
        coEvery { refreshRatings() } just Runs
        return WatchlistViewModel(
            getWatchlist, toggleWatchlist, refreshWatchlist,
            getAllRatings, refreshRatings, preferences,
        )
    }

    // --- Watchlist population ---

    @Test
    fun `watchlist flow populates movies and clears loading`() {
        val movies = listOf(fakeMovie(id = 1), fakeMovie(id = 2))
        every { getWatchlist() } returns flowOf(movies)
        every { getAllRatings() } returns flowOf(emptyMap())
        every { preferences.isListView } returns flowOf(false)
        coEvery { refreshWatchlist() } just Runs
        coEvery { refreshRatings() } just Runs

        val vm = WatchlistViewModel(
            getWatchlist, toggleWatchlist, refreshWatchlist,
            getAllRatings, refreshRatings, preferences,
        )

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(movies, vm.uiState.value.movies)
    }

    @Test
    fun `empty watchlist clears loading with empty list`() {
        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.movies.isEmpty())
    }

    @Test
    fun `ratings map from getAllRatings populates ratings state`() {
        val ratingsMap = mapOf(1 to 8f, 2 to 6f)
        every { getAllRatings() } returns flowOf(ratingsMap)
        every { getWatchlist() } returns flowOf(emptyList())
        every { preferences.isListView } returns flowOf(false)
        coEvery { refreshWatchlist() } just Runs
        coEvery { refreshRatings() } just Runs

        val vm = WatchlistViewModel(
            getWatchlist, toggleWatchlist, refreshWatchlist,
            getAllRatings, refreshRatings, preferences,
        )

        assertEquals(ratingsMap, vm.uiState.value.ratings)
    }

    // --- View mode ---

    @Test
    fun `init restores list view mode from preferences`() {
        every { preferences.isListView } returns flowOf(true)
        every { getWatchlist() } returns flowOf(emptyList())
        every { getAllRatings() } returns flowOf(emptyMap())
        coEvery { refreshWatchlist() } just Runs
        coEvery { refreshRatings() } just Runs

        val vm = WatchlistViewModel(
            getWatchlist, toggleWatchlist, refreshWatchlist,
            getAllRatings, refreshRatings, preferences,
        )

        assertEquals(WatchlistViewMode.List, vm.uiState.value.viewMode)
    }

    @Test
    fun `toggleViewMode switches from grid to list`() = runTest {
        val vm = createViewModel()
        coEvery { preferences.setListView(any()) } just Runs

        assertEquals(WatchlistViewMode.Grid, vm.uiState.value.viewMode)
        vm.onAction(WatchlistAction.ToggleViewMode)
        assertEquals(WatchlistViewMode.List, vm.uiState.value.viewMode)
    }

    @Test
    fun `toggleViewMode persists new mode via preferences`() = runTest {
        val vm = createViewModel()
        coEvery { preferences.setListView(any()) } just Runs

        vm.onAction(WatchlistAction.ToggleViewMode)

        coVerify { preferences.setListView(true) }
    }

    @Test
    fun `toggleViewMode switches back to grid on second toggle`() = runTest {
        val vm = createViewModel()
        coEvery { preferences.setListView(any()) } just Runs

        vm.onAction(WatchlistAction.ToggleViewMode)
        vm.onAction(WatchlistAction.ToggleViewMode)

        assertEquals(WatchlistViewMode.Grid, vm.uiState.value.viewMode)
        coVerify { preferences.setListView(false) }
    }

    // --- Sort ---

    @Test
    fun `selectSort updates sort in state`() {
        val vm = createViewModel()

        vm.onAction(WatchlistAction.SelectSort(WatchlistSort.TitleAZ))

        assertEquals(WatchlistSort.TitleAZ, vm.uiState.value.sort)
    }

    // --- Remove / undo ---

    @Test
    fun `removeMovie calls toggleWatchlist with isWatchlisted = true`() = runTest {
        val movie = fakeMovie(id = 42)
        every { getWatchlist() } returns flowOf(listOf(movie))
        every { getAllRatings() } returns flowOf(emptyMap())
        every { preferences.isListView } returns flowOf(false)
        coEvery { refreshWatchlist() } just Runs
        coEvery { refreshRatings() } just Runs
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = WatchlistViewModel(
            getWatchlist, toggleWatchlist, refreshWatchlist,
            getAllRatings, refreshRatings, preferences,
        )
        vm.onAction(WatchlistAction.RemoveMovie(movieId = 42))

        coVerify { toggleWatchlist(movie, isWatchlisted = true) }
    }

    @Test
    fun `removeMovie sends snackbar event`() = runTest {
        val movie = fakeMovie(id = 7)
        every { getWatchlist() } returns flowOf(listOf(movie))
        every { getAllRatings() } returns flowOf(emptyMap())
        every { preferences.isListView } returns flowOf(false)
        coEvery { refreshWatchlist() } just Runs
        coEvery { refreshRatings() } just Runs
        coEvery { toggleWatchlist(any(), any()) } just Runs

        val vm = WatchlistViewModel(
            getWatchlist, toggleWatchlist, refreshWatchlist,
            getAllRatings, refreshRatings, preferences,
        )

        vm.events.test {
            vm.onAction(WatchlistAction.RemoveMovie(movieId = 7))
            assertTrue(awaitItem() is WatchlistEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeMovie with unknown id does nothing`() = runTest {
        val vm = createViewModel()

        vm.onAction(WatchlistAction.RemoveMovie(movieId = 999))

        coVerify(exactly = 0) { toggleWatchlist(any(), any()) }
    }

    @Test
    fun `undoRemove calls toggleWatchlist with isWatchlisted = false`() = runTest {
        val movie = fakeMovie(id = 5)
        val vm = createViewModel()
        coEvery { toggleWatchlist(any(), any()) } just Runs

        vm.onAction(WatchlistAction.UndoRemove(movie))

        coVerify { toggleWatchlist(movie, isWatchlisted = false) }
    }
}
