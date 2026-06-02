# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Multi-module Android app using MVVM + Clean Architecture.
Package root: `com.template.android` · Min SDK: 24 · Compile SDK: 37
Build system: Gradle 9.4.1 · AGP 9.2.1 · Kotlin 2.2.10

---

## Common commands

```bash
./gradlew assembleDevDebug                        # build debug APK (dev flavor)
./gradlew assembleProdRelease                     # build release APK (prod flavor, minified)
./gradlew :app:testDevDebugUnitTest               # run app unit tests
./gradlew :app:connectedDevDebugAndroidTest       # run instrumented tests (device/emulator required)
./gradlew :<module>:testDebugUnitTest             # run unit tests for a non-app module (no flavor needed)
./gradlew clean                                   # wipe build artifacts
```

Note: `:app` has `dev` and `prod` flavors — all `:app` tasks require a flavor prefix (`Dev`/`Prod`).
Library and feature modules have no flavors; plain `testDebugUnitTest` works for them.

---

## Module graph

```
app
 ├── :feature:feature-*        (one module per screen group)
 ├── :core:core-ui
 └── :core:core-common

feature:feature-*
 ├── :core:core-ui
 └── :core:core-common

core:core-data
 ├── :core:core-domain
 ├── :core:core-common
 ├── :core:core-network
 └── :core:core-database

core:core-domain
 └── :core:core-common

core:core-datastore
 └── :core:core-common

core:core-testing          (testImplementation only — never ship)
 └── :core:core-common
```

**Hard rules — never break these:**
- Features never depend on other features.
- Features never depend on `:core:core-data`, `:core:core-network`, or `:core:core-database` directly.
- `:core:core-domain` is pure logic — no Android framework imports, no Hilt.
- `:app` only wires navigation and the Hilt application class; no business logic lives here.

---

## Module purposes

| Module | Purpose |
|---|---|
| `:app` | `MyApplication`, `MainActivity`, top-level `NavDisplay`, `AppModule` |
| `:core:core-common` | `Result<T>`, `AppDispatchers`, shared Kotlin utilities |
| `:core:core-domain` | `UseCase<P,R>`, `FlowUseCase<P,T>` base classes, repository interfaces |
| `:core:core-data` | Repository implementations, `networkBoundResource`, `safeApiCall` |
| `:core:core-network` | Retrofit + OkHttp setup, `NetworkModule` (Hilt), `ApiResponse`, `@BaseUrl` |
| `:core:core-database` | Room `AppDatabase`, `DatabaseModule` (Hilt), all `@Entity` classes |
| `:core:core-datastore` | `PreferencesRepository`, `DataStoreModule` (Hilt) |
| `:core:core-ui` | `AndroidTemplateTheme`, `ScreenScaffold`, `LoadingIndicator`, `ErrorContent`, `EmptyContent`, `RemoteImage`, preview utilities |
| `:core:core-testing` | `MainDispatcherRule`, `TestAppDispatchers` — `testImplementation` only |
| `:feature:feature-home` | Home screen — template for all future feature modules |

---

## Convention plugins

Defined in `build-logic/convention/src/main/kotlin/`. Applied by ID — no catalog entry needed.

| Plugin ID | Applies | Use on |
|---|---|---|
| `template.android.application` | `com.android.application` | `:app` only |
| `template.android.library` | `com.android.library` | all library modules |
| `template.android.compose` | `kotlin.plugin.compose` + `buildFeatures.compose` | any module with Compose UI |
| `template.android.hilt` | `hilt.android` + `ksp` + hilt deps | any module with `@Inject` / `@HiltViewModel` |

**Important — AGP 9.x notes:**
- Do NOT apply `org.jetbrains.kotlin.android` manually — AGP 9.x applies it automatically.
- `buildFeatures.compose` must be set via the `template.android.compose` plugin, not inline.
- `compileSdk`, `minSdk`, `targetSdk` are set centrally in the convention plugins from `libs.versions.toml` — don't override them per-module.

---

## Screen / ViewModel pattern

Every feature follows this exact split:

```
<Name>Screen.kt       — @Composable fun, calls hiltViewModel(), no logic
<Name>ViewModel.kt    — @HiltViewModel, extends BaseViewModel<Action, Event, UiState>
navigation/<Name>Navigation.kt — const val ROUTE + NavGraphBuilder extension
```

**Three types per feature:**
- **Action** — sealed interface of user intents sent from Compose to the VM
- **Event** — sealed interface of one-time side effects sent from VM to Compose (navigation, toasts)
- **UiState** — immutable data class representing the full screen state

**ViewModel template:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<HomeAction, HomeEvent, HomeUiState>(
    initialState = HomeUiState(),
) {
    override fun handleAction(action: HomeAction) {
        when (action) {
            HomeAction.LoadContent -> loadContent()
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            // call use case, then:
            updateState { copy(isLoading = false, message = "Hello") }
            // or for one-time side effects:
            sendEvent(HomeEvent.ShowSnackbar("Loaded"))
        }
    }
}

sealed interface HomeAction {
    data object LoadContent : HomeAction
}

sealed interface HomeEvent {
    data class ShowSnackbar(val message: String) : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val message: String = "",
)
```

**Screen template:**
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowSnackbar -> { /* wire to snackbarHostState */ }
            }
        }
    }

    HomeContent(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
private fun HomeContent(uiState: HomeUiState, onAction: (HomeAction) -> Unit) { ... }
```

**Navigation template:**
```kotlin
@Serializable
data object HomeKey                          // data class for destinations with args

fun EntryProviderBuilder.homeDestination() {
    entry<HomeKey> { HomeScreen() }
}
```

Register in `MainActivity` inside `entryProvider { }`: `homeDestination()`

Navigate to a destination: `backStack.add(HomeKey)`
Navigate back: `backStack.removeLastOrNull()`

**`BaseViewModel` lives in `:core:core-ui`** (`core/core-ui/src/main/java/com/template/android/core/ui/BaseViewModel.kt`). It exposes:
- `uiState: StateFlow<UiState>` — collect with `collectAsStateWithLifecycle()`
- `events: Flow<Event>` — collect inside `LaunchedEffect(Unit)` for one-time side effects
- `onAction(action)` — called by Compose; routes to `handleAction` via a buffered `SharedFlow`

---

## Skills

Detailed step-by-step recipes live in `.github/skills/`. Load the relevant file for the task:

| Task | Skill |
|---|---|
| Add a feature screen/module | `.github/skills/add-feature-module/SKILL.md` |
| Add a Room entity + DAO + repository | `.github/skills/add-room-entity/SKILL.md` |
| Add a Retrofit API call end-to-end | `.github/skills/add-api-call/SKILL.md` |
| Add a library dependency | `.github/skills/add-dependency/SKILL.md` |
| Write or set up tests | `.github/skills/android-testing/SKILL.md` |
| Load remote images with Coil | `.github/skills/add-image-loading/SKILL.md` |

---

## Key files

| File | Purpose |
|---|---|
| `gradle/libs.versions.toml` | Single source of truth for all versions and dependencies |
| `settings.gradle.kts` | Module includes + `includeBuild("build-logic")` |
| `build-logic/convention/src/main/kotlin/` | All four convention plugins |
| `core/core-common/src/main/java/.../Result.kt` | `Result<T>` — use for all async return types |
| `core/core-common/src/main/java/.../AppDispatchers.kt` | Inject for coroutine dispatcher control in tests |
| `core/core-data/src/main/java/.../NetworkBoundResource.kt` | Offline-first Flow helper |
| `core/core-domain/src/main/java/.../UseCase.kt` | Base class for all use cases |
| `core/core-ui/src/main/java/.../theme/Theme.kt` | App theme — `AndroidTemplateTheme` |
| `app/src/main/java/.../MainActivity.kt` | Single activity, hosts `NavHost` |

---

## What NOT to do

- Don't apply `org.jetbrains.kotlin.android` in any build file or convention plugin.
- Don't add `android.useAndroidX=true` to module-level `gradle.properties` — it's set globally.
- Don't put `@Entity` or `@Dao` classes outside `:core:core-database`.
- Don't put Retrofit service interfaces outside `:core:core-network`.
- Don't put business logic in `@Composable` functions or in `MainActivity`.
- Don't add direct dependencies between feature modules.
- Don't use `LiveData` — use `StateFlow` / `collectAsStateWithLifecycle()`.
- Don't extend `ViewModel` directly in feature modules — extend `BaseViewModel<Action, Event, UiState>`.
- Don't use a sealed interface for `UiState` — use a data class with default values so the VM can use `updateState { copy(...) }`.
- Don't use the deprecated `kotlinOptions { jvmTarget }` DSL — use `compilerOptions { jvmTarget.set(...) }`.
