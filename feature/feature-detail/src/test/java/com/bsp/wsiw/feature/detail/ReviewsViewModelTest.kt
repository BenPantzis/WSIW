package com.bsp.wsiw.feature.detail

import app.cash.turbine.test
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.domain.usecase.GetMovieReviewsUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
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

class ReviewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieReviews: GetMovieReviewsUseCase = mockk()

    private fun fakeReview(id: String = "r1") = Review(
        id = id,
        author = "Reviewer",
        avatarUrl = null,
        rating = 8f,
        content = "Great movie.",
        createdAt = "2024-01-01T00:00:00.000Z",
    )

    private fun pagedReviews(vararg reviews: Review, totalPages: Int = 1) =
        PagedResult(items = reviews.toList(), totalPages = totalPages)

    @Before
    fun setup() {
        every { getMovieReviews(any()) } returns flowOf(Result.Success(pagedReviews()))
    }

    private fun createViewModel(movieId: Int = 1) = ReviewsViewModel(movieId, getMovieReviews)

    @Test
    fun `initial load success clears loading and populates reviews`() {
        val review = fakeReview()
        every { getMovieReviews(any()) } returns flowOf(Result.Success(pagedReviews(review)))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(listOf(review), vm.uiState.value.reviews)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `initial load error with no cached reviews sets error state`() {
        every { getMovieReviews(any()) } returns flowOf(Result.Error(RuntimeException("fail")))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.reviews.isEmpty())
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `retry calls source a second time`() {
        val vm = createViewModel()
        vm.onAction(ReviewsAction.Retry)

        verify(atLeast = 2) { getMovieReviews(any()) }
    }

    @Test
    fun `load next page appends reviews`() {
        val page1 = listOf(fakeReview("r1"), fakeReview("r2"))
        val page2 = listOf(fakeReview("r3"), fakeReview("r4"))
        var callCount = 0
        every { getMovieReviews(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(pagedReviews(*page1.toTypedArray(), totalPages = 3)))
            else flowOf(Result.Success(pagedReviews(*page2.toTypedArray(), totalPages = 3)))
        }

        val vm = createViewModel()
        assertEquals(page1, vm.uiState.value.reviews)

        vm.onAction(ReviewsAction.LoadNextPage)

        assertEquals(page1 + page2, vm.uiState.value.reviews)
        assertEquals(2, vm.uiState.value.currentPage)
    }

    @Test
    fun `load next page is no-op when already at last page`() {
        every { getMovieReviews(any()) } returns flowOf(Result.Success(pagedReviews(totalPages = 1)))

        val vm = createViewModel()
        vm.onAction(ReviewsAction.LoadNextPage)

        verify(exactly = 1) { getMovieReviews(any()) }
    }

    @Test
    fun `error on pagination with cached reviews sends snackbar and preserves reviews`() = runTest {
        val existing = listOf(fakeReview("r1"), fakeReview("r2"))
        var callCount = 0
        every { getMovieReviews(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(pagedReviews(*existing.toTypedArray(), totalPages = 3)))
            else flowOf(Result.Error(RuntimeException("page fail")))
        }

        val vm = createViewModel()
        assertEquals(existing, vm.uiState.value.reviews)

        vm.events.test {
            vm.onAction(ReviewsAction.LoadNextPage)
            assertTrue(awaitItem() is ReviewsEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(existing, vm.uiState.value.reviews)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `select sort changes sort without reloading`() {
        val vm = createViewModel()
        vm.onAction(ReviewsAction.SelectSort(ReviewSort.Oldest))

        assertEquals(ReviewSort.Oldest, vm.uiState.value.sort)
        verify(exactly = 1) { getMovieReviews(any()) }
    }
}
