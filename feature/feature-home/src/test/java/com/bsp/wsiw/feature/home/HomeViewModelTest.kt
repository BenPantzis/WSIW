package com.bsp.wsiw.feature.home

import app.cash.turbine.test
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.usecase.DiscoverMoviesUseCase
import com.bsp.wsiw.core.domain.usecase.GetGenresUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import com.bsp.wsiw.core.ui.UiText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val discoverMovies: DiscoverMoviesUseCase = mockk()
    private val getGenres: GetGenresUseCase = mockk()

    @Before
    fun setup() {
        every { discoverMovies(any()) } returns flowOf(Result.Success(PagedResult(items = emptyList(), totalPages = 1)))
        every { getGenres() } returns flowOf(Result.Success(emptyList()))
    }

    private fun createViewModel() = HomeViewModel(discoverMovies, getGenres)

    @Test
    fun `success populates movies and clears loading`() {
        val movies = listOf(fakeMovie(id = 1), fakeMovie(id = 2))
        every { discoverMovies(any()) } returns
            flowOf(Result.Success(PagedResult(items = movies, totalPages = 1)))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(movies, vm.uiState.value.movies)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `error without cached movies sets error string`() {
        every { discoverMovies(any()) } returns
            flowOf(Result.Error(RuntimeException("Network error")))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
        assertTrue(vm.uiState.value.movies.isEmpty())
    }

    @Test
    fun `error with null message uses fallback`() {
        every { discoverMovies(any()) } returns
            flowOf(Result.Error(null))

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `error with cached movies sends snackbar and preserves movies`() = runTest {
        val movies = listOf(fakeMovie())
        var callCount = 0
        every { discoverMovies(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(PagedResult(items = movies, totalPages = 1)))
            else flowOf(Result.Error(RuntimeException("No connection")))
        }

        val vm = createViewModel()

        vm.events.test {
            vm.onAction(HomeAction.Refresh)
            assertTrue(awaitItem() is HomeEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(movies, vm.uiState.value.movies)
    }

    @Test
    fun `refresh calls source a second time`() {
        val movies = listOf(fakeMovie())
        every { discoverMovies(any()) } returns
            flowOf(Result.Success(PagedResult(items = movies, totalPages = 1)))

        val vm = createViewModel()
        vm.onAction(HomeAction.Refresh)

        verify(atLeast = 2) { discoverMovies(any()) }
    }

    @Test
    fun `retry calls source a second time`() {
        every { discoverMovies(any()) } returns
            flowOf(Result.Success(PagedResult(items = emptyList(), totalPages = 1)))

        val vm = createViewModel()
        vm.onAction(HomeAction.Retry)

        verify(atLeast = 2) { discoverMovies(any()) }
    }

    @Test
    fun `load next page appends movies`() {
        val page1 = listOf(fakeMovie(id = 1), fakeMovie(id = 2))
        val page2 = listOf(fakeMovie(id = 3), fakeMovie(id = 4))
        var callCount = 0
        every { discoverMovies(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(PagedResult(items = page1, totalPages = 5)))
            else flowOf(Result.Success(PagedResult(items = page2, totalPages = 5)))
        }

        val vm = createViewModel()
        assertEquals(page1, vm.uiState.value.movies)
        assertEquals(1, vm.uiState.value.currentPage)

        vm.onAction(HomeAction.LoadNextPage)

        assertEquals(page1 + page2, vm.uiState.value.movies)
        assertEquals(2, vm.uiState.value.currentPage)
    }

    @Test
    fun `load next page is no-op when already at last page`() {
        val movies = listOf(fakeMovie())
        every { discoverMovies(any()) } returns
            flowOf(Result.Success(PagedResult(items = movies, totalPages = 1)))

        val vm = createViewModel()
        vm.onAction(HomeAction.LoadNextPage)

        verify(exactly = 1) { discoverMovies(any()) }
    }

    @Test
    fun `category switch resets movies and page`() {
        val trendingMovies = listOf(fakeMovie(id = 1))
        val topRatedMovies = listOf(fakeMovie(id = 2))
        var callCount = 0
        every { discoverMovies(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(PagedResult(items = trendingMovies, totalPages = 1)))
            else flowOf(Result.Success(PagedResult(items = topRatedMovies, totalPages = 1)))
        }

        val vm = createViewModel()
        assertEquals(trendingMovies, vm.uiState.value.movies)

        vm.onAction(HomeAction.SelectCategory(HomeCategory.TopRated))

        assertEquals(topRatedMovies, vm.uiState.value.movies)
        assertEquals(1, vm.uiState.value.currentPage)
    }

    @Test
    fun `success after loading clears error from previous attempt`() {
        val movies = listOf(fakeMovie())
        var callCount = 0
        every { discoverMovies(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Error(RuntimeException("fail")))
            else flowOf(Result.Success(PagedResult(items = movies, totalPages = 1)))
        }

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.error)

        vm.onAction(HomeAction.Retry)

        assertNull(vm.uiState.value.error)
        assertEquals(movies, vm.uiState.value.movies)
    }
}
