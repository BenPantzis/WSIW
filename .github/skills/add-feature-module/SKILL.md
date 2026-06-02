---
name: add-feature-module
description: Scaffold a complete new feature module — Screen, ViewModel, Navigation, and NavDisplay registration — following the BaseViewModel<Action, Event, UiState> pattern and Navigation 3.
metadata:
  author: com.template.android
  last-updated: '2026-06-01'
  keywords: [feature module, MVVM, Jetpack Compose, Hilt, Navigation 3, multi-module]
---

## Overview

Creates a full feature module under `feature/feature-<name>/`. Uses `BaseViewModel<Action, Event, UiState>` from `:core:core-ui`, Hilt via the `template.android.hilt` convention plugin, Compose via `template.android.compose`, and Navigation 3 for type-safe destination keys.

## Hard constraints

- Features never depend on other features.
- Features never depend on `:core:core-data`, `:core:core-network`, or `:core:core-database` directly.
- No business logic in `@Composable` functions — delegate entirely to the ViewModel.

## Steps

### 1. Create the directory structure

```
feature/feature-<name>/
  build.gradle.kts
  src/main/AndroidManifest.xml
  src/main/java/com/template/android/feature/<name>/
    <Name>Screen.kt
    <Name>ViewModel.kt
    navigation/<Name>Navigation.kt
```

### 2. `build.gradle.kts`

```kotlin
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.compose")
    id("template.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android { namespace = "com.template.android.feature.<name>" }

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(catalog.findLibrary("androidx-lifecycle-viewmodel").get())
    implementation(catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
    implementation(catalog.findLibrary("androidx-navigation3-runtime").get())
    implementation(catalog.findLibrary("hilt-navigation-compose").get())
    implementation(catalog.findLibrary("kotlinx-serialization-core").get())
}
```

### 3. `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

### 4. `<Name>ViewModel.kt`

```kotlin
@HiltViewModel
class <Name>ViewModel @Inject constructor() : BaseViewModel<
    <Name>Action, <Name>Event, <Name>UiState
>(initialState = <Name>UiState()) {

    override fun handleAction(action: <Name>Action) {
        when (action) {
            <Name>Action.LoadContent -> loadContent()
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            // TODO: call use case
            updateState { copy(isLoading = false) }
        }
    }
}

sealed interface <Name>Action {
    data object LoadContent : <Name>Action
}

sealed interface <Name>Event {
    data class ShowSnackbar(val message: String) : <Name>Event
}

data class <Name>UiState(
    val isLoading: Boolean = true,
)
```

### 5. `<Name>Screen.kt`

```kotlin
@Composable
fun <Name>Screen(viewModel: <Name>ViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is <Name>Event.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    <Name>Content(
        uiState = uiState,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun <Name>Content(
    uiState: <Name>UiState,
    onAction: (<Name>Action) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    ScreenScaffold(snackbarHostState = snackbarHostState) { padding, _ ->
        // TODO: implement UI — use Modifier.padding(padding) on top-level content
    }
}
```

### 6. `navigation/<Name>Navigation.kt`

```kotlin
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
// Note: do NOT import entry — it is a member of EntryProviderScope

@Serializable
data object <Name>Key : NavKey

fun EntryProviderScope<NavKey>.<name>Destination() {
    entry<<Name>Key> { <Name>Screen() }
}
```

To navigate **to** this destination from another screen, add `<Name>Key` to the back stack:
```kotlin
backStack.add(<Name>Key)
```

To navigate **with arguments**, make `<Name>Key` a data class:
```kotlin
@Serializable
data class <Name>Key(val id: String) : NavKey
```

### 7. Register in `settings.gradle.kts`

Add: `include(":feature:feature-<name>")`

### 8. Register in `app/build.gradle.kts`

Add: `implementation(project(":feature:feature-<name>"))`

### 9. Register in `app/src/main/java/com/template/android/MainActivity.kt`

Inside the `entryProvider { }` block, add: `<name>Destination()`

## Verification

```bash
./gradlew :feature:feature-<name>:assembleDebug   # module compiles (no flavor needed for feature modules)
./gradlew assembleDevDebug                         # full app builds
```
