package com.template.android.core.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.template.android.core.ui.theme.AndroidTemplateTheme

@PreviewLightDark
annotation class AndroidTemplatePreview

@Composable
fun AndroidTemplatePreviewWrapper(content: @Composable () -> Unit) {
    AndroidTemplateTheme {
        Surface { content() }
    }
}
