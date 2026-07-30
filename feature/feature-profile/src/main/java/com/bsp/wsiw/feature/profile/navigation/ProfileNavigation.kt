@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.profile.LoginCallbackScreen
import com.bsp.wsiw.feature.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : NavKey

@Serializable
data class LoginCallbackKey(val requestToken: String) : NavKey

fun EntryProviderScope<NavKey>.profileDestination() {
    entry<ProfileKey> {
        ProfileScreen()
    }
}

fun EntryProviderScope<NavKey>.loginCallbackDestination(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    entry<LoginCallbackKey> { key ->
        LoginCallbackScreen(
            requestToken = key.requestToken,
            onSuccess = onSuccess,
            onBack = onBack,
        )
    }
}
