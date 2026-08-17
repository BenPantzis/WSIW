package com.bsp.wsiw.feature.tv

import app.cash.turbine.test
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.usecase.DiscoverTvUseCase
import com.bsp.wsiw.core.domain.usecase.GetTvByCategoryUseCase
import com.bsp.wsiw.core.domain.usecase.GetTvGenresUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeTvShow
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

class TvListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val discoverTv: DiscoverTvUseCase = mockk()
    private val getTvByCategory: GetTvByCategoryUseCase = mockk()
    private val getTvGenres: GetTvGenresUseCase = mockk()

    @Before
    fun setup() {
        // Default state: Trending category, default filter → getTvByCategory is called
        every { getTvByCategory(any()) } returns flowOf(Result.Success(PagedResult(items = emptyList(), totalPages = 1)))
        every { discoverTv(any()) } returns flowOf(Result.Success(PagedResult(items = emptyList(), totalPages = 1)))
        every { getTvGenres() } returns flowOf(Result.Success(emptyList()))
    }

    private fun createViewModel() = TvListViewModel(discoverTv, getTvByCategory, getTvGenres)

    @Test
    fun `success populates shows and clears loading`() {
        val shows = listOf(fakeTvShow(id = 1), fakeTvShow(id = 2))
        every { getTvByCategory(any()) } returns
            flowOf(Result.Success(PagedResult(items = shows, totalPages = 1)))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(shows, vm.uiState.value.shows)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `error without cached shows sets error string`() {
        every { getTvByCategory(any()) } returns
            flowOf(Result.Error(RuntimeException("Network error")))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
        assertTrue(vm.uiState.value.shows.isEmpty())
    }

    @Test
    fun `error with null exception uses fallback`() {
        every { getTvByCategory(any()) } returns
            flowOf(Result.Error(null))

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `error with cached shows sends snackbar and preserves shows`() = runTest {
        val shows = listOf(fakeTvShow())
        var callCount = 0
        every { getTvByCategory(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(PagedResult(items = shows, totalPages = 1)))
            else flowOf(Result.Error(RuntimeException("No connection")))
        }

        val vm = createViewModel()

        vm.events.test {
            vm.onAction(TvListAction.Refresh)
            assertTrue(awaitItem() is TvListEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(shows, vm.uiState.value.shows)
    }

    @Test
    fun `refresh calls source a second time`() {
        val shows = listOf(fakeTvShow())
        every { getTvByCategory(any()) } returns
            flowOf(Result.Success(PagedResult(items = shows, totalPages = 1)))

        val vm = createViewModel()
        vm.onAction(TvListAction.Refresh)

        verify(atLeast = 2) { getTvByCategory(any()) }
    }

    @Test
    fun `retry calls source a second time`() {
        val vm = createViewModel()
        vm.onAction(TvListAction.Retry)

        verify(atLeast = 2) { getTvByCategory(any()) }
    }

    @Test
    fun `load next page appends shows`() {
        val page1 = listOf(fakeTvShow(id = 1), fakeTvShow(id = 2))
        val page2 = listOf(fakeTvShow(id = 3), fakeTvShow(id = 4))
        var callCount = 0
        every { getTvByCategory(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(PagedResult(items = page1, totalPages = 5)))
            else flowOf(Result.Success(PagedResult(items = page2, totalPages = 5)))
        }

        val vm = createViewModel()
        assertEquals(page1, vm.uiState.value.shows)
        assertEquals(1, vm.uiState.value.currentPage)

        vm.onAction(TvListAction.LoadNextPage)

        assertEquals(page1 + page2, vm.uiState.value.shows)
        assertEquals(2, vm.uiState.value.currentPage)
    }

    @Test
    fun `load next page is no-op when already at last page`() {
        val shows = listOf(fakeTvShow())
        every { getTvByCategory(any()) } returns
            flowOf(Result.Success(PagedResult(items = shows, totalPages = 1)))

        val vm = createViewModel()
        vm.onAction(TvListAction.LoadNextPage)

        verify(exactly = 1) { getTvByCategory(any()) }
    }

    @Test
    fun `category switch resets shows and page`() {
        val trending = listOf(fakeTvShow(id = 1))
        val topRated = listOf(fakeTvShow(id = 2))
        var callCount = 0
        every { getTvByCategory(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(PagedResult(items = trending, totalPages = 1)))
            else flowOf(Result.Success(PagedResult(items = topRated, totalPages = 1)))
        }

        val vm = createViewModel()
        assertEquals(trending, vm.uiState.value.shows)

        vm.onAction(TvListAction.SelectCategory(TvCategory.TopRated))

        assertEquals(topRated, vm.uiState.value.shows)
        assertEquals(1, vm.uiState.value.currentPage)
    }

    @Test
    fun `success after error clears error state`() {
        val shows = listOf(fakeTvShow())
        var callCount = 0
        every { getTvByCategory(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Error(RuntimeException("fail")))
            else flowOf(Result.Success(PagedResult(items = shows, totalPages = 1)))
        }

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.error)

        vm.onAction(TvListAction.Retry)

        assertNull(vm.uiState.value.error)
        assertEquals(shows, vm.uiState.value.shows)
    }
}
