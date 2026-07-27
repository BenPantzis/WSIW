@file:Suppress("MatchingDeclarationName")

package com.bsp.wsiw.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.bsp.wsiw.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

fun EntryProviderScope<NavKey>.homeDestination() {
    entry<HomeKey> { HomeScreen() }
}
