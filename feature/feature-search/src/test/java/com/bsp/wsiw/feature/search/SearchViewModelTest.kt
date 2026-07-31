package com.bsp.wsiw.feature.search

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.domain.usecase.SearchMoviesUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import com.bsp.wsiw.core.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    // StandardTestDispatcher is required here so we can manually advance virtual time
    // past the debounce delay. Both the rule and runTest share the same dispatcher
    // instance so advanceTimeBy affects the ViewModel's coroutines.
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val searchMovies: SearchMoviesUseCase = mockk()
    private val movieRepository: MovieRepository = mockk(relaxed = true) {
        every { getMoviesByCategory(any(), any()) } returns flowOf(
            Result.Success(PagedResult(emptyList(), totalPages = 1))
        )
    }

    private fun createViewModel() = SearchViewModel(searchMovies, movieRepository)

    @Test
    fun `initial state has empty query and no results`() {
        val vm = createViewModel()

        assertEquals("", vm.uiState.value.query)
        assertTrue(vm.uiState.value.movies.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `updateQuery immediately clears stale movies and error`() = runTest(testDispatcher) {
        val movies = listOf(fakeMovie())
        every { searchMovies(any()) } returns flowOf(Result.Success(movies))

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))
        advanceTimeBy(400)

        assertEquals("batman", vm.uiState.value.query)
        assertEquals(movies, vm.uiState.value.movies)

        // Changing query mid-search should instantly clear stale results
        vm.onAction(SearchAction.UpdateQuery("sup"))

        assertEquals("sup", vm.uiState.value.query)
        assertTrue(vm.uiState.value.movies.isEmpty())
    }

    @Test
    fun `clearQuery resets to initial state`() = runTest(testDispatcher) {
        val movies = listOf(fakeMovie())
        every { searchMovies(any()) } returns flowOf(Result.Success(movies))

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))
        advanceTimeBy(400)
        assertEquals(movies, vm.uiState.value.movies)

        vm.onAction(SearchAction.ClearQuery)

        assertEquals("", vm.uiState.value.query)
        assertTrue(vm.uiState.value.movies.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `blank query does not call search use case`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("   "))
        advanceTimeBy(400)

        verify(exactly = 0) { searchMovies(any()) }
    }

    @Test
    fun `non-blank query triggers search after debounce and shows results`() = runTest(testDispatcher) {
        val movies = listOf(fakeMovie(id = 10, title = "Batman"))
        every { searchMovies("batman") } returns flowOf(Result.Success(movies))

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))

        // Before debounce: no results yet
        assertTrue(vm.uiState.value.movies.isEmpty())

        advanceTimeBy(400)

        assertEquals(movies, vm.uiState.value.movies)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `search error sets error in state`() = runTest(testDispatcher) {
        every { searchMovies(any()) } returns flowOf(
            Result.Error(RuntimeException("Search failed"))
        )

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))
        advanceTimeBy(400)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
        assertTrue(vm.uiState.value.movies.isEmpty())
    }
}
