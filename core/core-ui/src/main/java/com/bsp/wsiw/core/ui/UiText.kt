package com.bsp.wsiw.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class StringResource(@param:StringRes val id: Int) : UiText
    data class Plain(val value: String) : UiText

    fun resolve(context: Context): String = when (this) {
        is StringResource -> context.getString(id)
        is Plain -> value
    }

    @Composable
    fun asString(): String = when (this) {
        is StringResource -> stringResource(id)
        is Plain -> value
    }
}
