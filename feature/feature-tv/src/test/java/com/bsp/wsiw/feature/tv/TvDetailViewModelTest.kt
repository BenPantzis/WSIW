package com.bsp.wsiw.feature.tv

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.usecase.GetTvDetailUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.testing.fakeTvShowDetail
import com.bsp.wsiw.core.ui.UiText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TvDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTvDetail: GetTvDetailUseCase = mockk()

    @Before
    fun setup() {
        every { getTvDetail(any()) } returns flowOf(Result.Success(fakeTvShowDetail()))
    }

    private fun createViewModel() = TvDetailViewModel(getTvDetail)

    @Test
    fun `initial state is loading without show`() {
        val vm = createViewModel()

        assertTrue(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.show)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `load success sets show and clears loading`() {
        val detail = fakeTvShowDetail()
        every { getTvDetail(any()) } returns flowOf(Result.Success(detail))

        val vm = createViewModel()
        vm.load(1)

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(detail, vm.uiState.value.show)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `load error sets error state`() {
        every { getTvDetail(any()) } returns flowOf(Result.Error(RuntimeException("Not found")))

        val vm = createViewModel()
        vm.load(1)

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.show)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `load with same id is no-op`() {
        val vm = createViewModel()
        vm.load(1)
        vm.load(1)

        verify(exactly = 1) { getTvDetail(any()) }
    }

    @Test
    fun `load with different id replaces show`() {
        val show1 = fakeTvShowDetail(id = 1, name = "Show One")
        val show2 = fakeTvShowDetail(id = 2, name = "Show Two")
        var callCount = 0
        every { getTvDetail(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Success(show1))
            else flowOf(Result.Success(show2))
        }

        val vm = createViewModel()
        vm.load(1)
        assertEquals(show1, vm.uiState.value.show)

        vm.load(2)
        assertEquals(show2, vm.uiState.value.show)
    }

    @Test
    fun `retry triggers second fetch`() {
        val vm = createViewModel()
        vm.load(1)

        vm.onAction(TvDetailAction.Retry)

        verify(exactly = 2) { getTvDetail(any()) }
    }
}
