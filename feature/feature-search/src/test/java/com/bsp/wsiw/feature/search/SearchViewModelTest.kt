package com.bsp.wsiw.feature.search

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.SearchResult
import com.bsp.wsiw.core.domain.usecase.GetTrendingMoviesUseCase
import com.bsp.wsiw.core.domain.usecase.MultiSearchUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeMovie
import com.bsp.wsiw.core.ui.UiText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    // StandardTestDispatcher is required here so we can manually advance virtual time
    // past the debounce delay. Both the rule and runTest share the same dispatcher
    // instance so advanceTimeBy affects the ViewModel's coroutines.
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val multiSearch: MultiSearchUseCase = mockk()
    private val getTrendingMovies: GetTrendingMoviesUseCase = mockk()

    @Before
    fun setup() {
        every { getTrendingMovies(any()) } returns flowOf(
            Result.Success(PagedResult(emptyList(), totalPages = 1))
        )
    }

    private fun createViewModel() = SearchViewModel(multiSearch, getTrendingMovies)

    @Test
    fun `initial state has empty query and no results`() {
        val vm = createViewModel()

        assertEquals("", vm.uiState.value.query)
        assertTrue(vm.uiState.value.results.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `updateQuery immediately clears stale results and error`() = runTest(testDispatcher) {
        val results = listOf(SearchResult.MovieResult(fakeMovie()))
        every { multiSearch(any()) } returns flowOf(Result.Success(results))

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))
        advanceTimeBy(400)

        assertEquals("batman", vm.uiState.value.query)
        assertEquals(results, vm.uiState.value.results)

        // Changing query mid-search should instantly clear stale results
        vm.onAction(SearchAction.UpdateQuery("sup"))

        assertEquals("sup", vm.uiState.value.query)
        assertTrue(vm.uiState.value.results.isEmpty())
    }

    @Test
    fun `clearQuery resets to initial state`() = runTest(testDispatcher) {
        val results = listOf(SearchResult.MovieResult(fakeMovie()))
        every { multiSearch(any()) } returns flowOf(Result.Success(results))

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))
        advanceTimeBy(400)
        assertEquals(results, vm.uiState.value.results)

        vm.onAction(SearchAction.ClearQuery)

        assertEquals("", vm.uiState.value.query)
        assertTrue(vm.uiState.value.results.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `blank query does not call search use case`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("   "))
        advanceTimeBy(400)

        verify(exactly = 0) { multiSearch(any()) }
    }

    @Test
    fun `non-blank query triggers search after debounce and shows results`() = runTest(testDispatcher) {
        val results = listOf(SearchResult.MovieResult(fakeMovie(id = 10, title = "Batman")))
        every { multiSearch("batman") } returns flowOf(Result.Success(results))

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))

        // Before debounce: no results yet
        assertTrue(vm.uiState.value.results.isEmpty())

        advanceTimeBy(400)

        assertEquals(results, vm.uiState.value.results)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `search error sets error in state`() = runTest(testDispatcher) {
        every { multiSearch(any()) } returns flowOf(
            Result.Error(RuntimeException("Search failed"))
        )

        val vm = createViewModel()
        vm.onAction(SearchAction.UpdateQuery("batman"))
        advanceTimeBy(400)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
        assertTrue(vm.uiState.value.results.isEmpty())
    }
}
