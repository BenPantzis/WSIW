package com.bsp.wsiw.feature.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.bsp.wsiw.core.ui.theme.WSIWTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeLoading() {
        composeTestRule.setContent {
            WSIWTheme {
                HomeContent(
                    uiState = HomeUiState(isLoading = true),
                    onAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun homeContent() {
        composeTestRule.setContent {
            WSIWTheme {
                HomeContent(
                    uiState = HomeUiState(isLoading = false),
                    onAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
