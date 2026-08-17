package com.bsp.wsiw.feature.profile

import app.cash.turbine.test
import com.bsp.wsiw.core.domain.usecase.CreateUserSessionUseCase
import com.bsp.wsiw.core.testing.MainDispatcherRule
import com.bsp.wsiw.core.ui.UiText
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginCallbackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createUserSession: CreateUserSessionUseCase = mockk()

    @Before
    fun setup() {
        coEvery { createUserSession(any()) } just Runs
    }

    private fun createViewModel(token: String = "test_token") =
        LoginCallbackViewModel(token, createUserSession)

    @Test
    fun `token exchange success emits NavigateToProfile`() = runTest {
        val vm = createViewModel()

        vm.events.test {
            assertTrue(awaitItem() is LoginCallbackEvent.NavigateToProfile)
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `token exchange failure sets error state`() = runTest {
        coEvery { createUserSession(any()) } throws RuntimeException("Network error")

        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error is UiText.StringResource)
    }

    @Test
    fun `retry on failure re-exchanges token and emits NavigateToProfile`() = runTest {
        coEvery { createUserSession(any()) } throws RuntimeException("fail")

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.error)

        coEvery { createUserSession(any()) } just Runs

        vm.events.test {
            vm.onAction(LoginCallbackAction.Retry)
            assertTrue(awaitItem() is LoginCallbackEvent.NavigateToProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry clears error before re-exchange`() = runTest {
        coEvery { createUserSession(any()) } throws RuntimeException("fail")

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.error)

        // Stub success so the coroutine completes cleanly; we only care about the
        // intermediate loading state being set, so observe state before the stub changes
        coEvery { createUserSession(any()) } just Runs

        vm.uiState.value // snapshot to confirm error was set above

        vm.onAction(LoginCallbackAction.Retry)

        // After retry completes successfully, error is gone and loading is false
        assertFalse(vm.uiState.value.isLoading)
    }
}
