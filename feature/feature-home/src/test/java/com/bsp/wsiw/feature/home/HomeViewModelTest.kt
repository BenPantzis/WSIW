package com.bsp.wsiw.feature.home

import app.cash.turbine.test
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.domain.usecase.GetPopularMoviesUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPopularMovies: GetPopularMoviesUseCase = mockk()
    private val movieRepository: MovieRepository = mockk(relaxed = true)

    private fun createViewModel() = HomeViewModel(getPopularMovies, movieRepository)

    @Test
    fun `success populates movies and clears loading`() {
        val movies = listOf(fakeMovie(id = 1), fakeMovie(id = 2))
        every { getPopularMovies(any()) } returns flowOf(Result.Success(movies))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(movies, vm.uiState.value.movies)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `error without cached movies sets error string`() {
        every { getPopularMovies(any()) } returns flowOf(
            Result.Error(RuntimeException("Network error"))
        )

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Network error", vm.uiState.value.error)
        assertTrue(vm.uiState.value.movies.isEmpty())
    }

    @Test
    fun `error with null message uses fallback`() {
        every { getPopularMovies(any()) } returns flowOf(Result.Error(null))

        val vm = createViewModel()

        assertEquals("Something went wrong", vm.uiState.value.error)
    }

    @Test
    fun `error with cached movies sends snackbar and preserves movies`() = runTest {
        val movies = listOf(fakeMovie())
        var callCount = 0
        every { getPopularMovies(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(movies))
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
    fun `refresh passes forceRefresh = true to use case`() {
        val movies = listOf(fakeMovie())
        val capturedParams = mutableListOf<GetPopularMoviesUseCase.Params>()
        every { getPopularMovies(any()) } answers {
            capturedParams.add(firstArg())
            flowOf(Result.Success(movies))
        }

        val vm = createViewModel()
        vm.onAction(HomeAction.Refresh)

        assertTrue(capturedParams.any { it.forceRefresh })
    }

    @Test
    fun `retry calls use case a second time`() {
        every { getPopularMovies(any()) } returns flowOf(Result.Success(emptyList()))

        val vm = createViewModel()
        vm.onAction(HomeAction.Retry)

        verify(exactly = 2) { getPopularMovies(any()) }
    }

    @Test
    fun `empty success with isRefreshing stays in shimmer`() {
        every { getPopularMovies(any()) } returns flowOf(
            Result.Success(data = emptyList(), isRefreshing = true)
        )

        val vm = createViewModel()

        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun `success after loading clears error from previous attempt`() {
        val movies = listOf(fakeMovie())
        var callCount = 0
        every { getPopularMovies(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Error(RuntimeException("fail")))
            else flowOf(Result.Success(movies))
        }

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.error)

        vm.onAction(HomeAction.Retry)

        assertNull(vm.uiState.value.error)
        assertEquals(movies, vm.uiState.value.movies)
    }
}
