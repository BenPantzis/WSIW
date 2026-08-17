package com.bsp.wsiw.feature.detail

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.PersonDetail
import com.bsp.wsiw.core.domain.usecase.GetPersonDetailUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
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

class PersonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPersonDetail: GetPersonDetailUseCase = mockk()

    private fun fakePerson(id: Int = 1) = PersonDetail(
        id = id,
        name = "Test Actor",
        biography = "A bio.",
        birthday = "1980-01-01",
        placeOfBirth = "Los Angeles",
        profileUrl = null,
        knownForDepartment = "Acting",
        filmography = emptyList(),
    )

    @Before
    fun setup() {
        every { getPersonDetail(any()) } returns flowOf(Result.Success(fakePerson()))
    }

    private fun createViewModel(personId: Int = 1) = PersonViewModel(personId, getPersonDetail)

    @Test
    fun `load success sets person and clears loading`() {
        val person = fakePerson()
        every { getPersonDetail(any()) } returns flowOf(Result.Success(person))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(person, vm.uiState.value.person)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `load error sets error state`() {
        every { getPersonDetail(any()) } returns flowOf(Result.Error(RuntimeException("Not found")))

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.person)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `retry calls use case a second time`() {
        val vm = createViewModel()
        vm.onAction(PersonAction.Retry)

        verify(atLeast = 2) { getPersonDetail(any()) }
    }

    @Test
    fun `retry after error clears error and loads person`() {
        var callCount = 0
        every { getPersonDetail(any()) } answers {
            if (callCount++ == 0) flowOf(Result.Error(RuntimeException("fail")))
            else flowOf(Result.Success(fakePerson()))
        }

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.error)

        vm.onAction(PersonAction.Retry)

        assertNull(vm.uiState.value.error)
        assertNotNull(vm.uiState.value.person)
        assertFalse(vm.uiState.value.isLoading)
    }
}
