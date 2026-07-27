package com.bsp.wsiw.core.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.bsp.wsiw.core.ui.theme.WSIWTheme

@PreviewLightDark
annotation class WSIWPreview

@Composable
fun WSIWPreviewWrapper(content: @Composable () -> Unit) {
    WSIWTheme {
        Surface { content() }
    }
}
