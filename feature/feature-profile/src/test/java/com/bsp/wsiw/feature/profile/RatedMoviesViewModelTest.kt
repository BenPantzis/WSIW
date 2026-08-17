package com.bsp.wsiw.feature.profile

import com.bsp.wsiw.core.domain.usecase.GetLocalRatedMoviesUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RatedMoviesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLocalRatedMovies: GetLocalRatedMoviesUseCase = mockk()

    @Before
    fun setup() {
        every { getLocalRatedMovies() } returns flowOf(emptyList())
    }

    private fun createViewModel() = RatedMoviesViewModel(getLocalRatedMovies)

    @Test
    fun `initial state has empty movies list and is not loading`() {
        val vm = createViewModel()

        assertTrue(vm.uiState.value.movies.isEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `rated movies are reflected in state`() {
        val movies = listOf(fakeMovie(id = 1) to 8f, fakeMovie(id = 2) to 6f)
        every { getLocalRatedMovies() } returns flowOf(movies)

        val vm = createViewModel()

        assertEquals(2, vm.uiState.value.movies.size)
        assertEquals(movies, vm.uiState.value.movies)
    }

    @Test
    fun `single rated movie with correct rating`() {
        val movie = fakeMovie(id = 42, title = "Inception")
        every { getLocalRatedMovies() } returns flowOf(listOf(movie to 9f))

        val vm = createViewModel()

        assertEquals(1, vm.uiState.value.movies.size)
        assertEquals(movie, vm.uiState.value.movies.first().first)
        assertEquals(9f, vm.uiState.value.movies.first().second)
    }
}
