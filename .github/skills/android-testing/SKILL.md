---
name: android-testing
description: Set up and write unit, integration, and Compose UI tests for this Hilt + Compose + Room + multi-module project using JUnit4, MockK, Turbine, and coroutines-test.
metadata:
  author: com.template.android
  last-updated: '2026-05-23'
  keywords: [testing, JUnit4, MockK, Turbine, Hilt, Compose, Room, coroutines-test, Robolectric]
---

## Overview

Three-layer pyramid: **Unit** (ViewModel, UseCase, Repo logic) → **Integration** (Room in-memory, MockWebServer) → **UI** (Compose + Hilt).

## Test dependencies

All test deps (`junit`, `mockk`, `turbine`, `coroutines-test`) are declared in `gradle/libs.versions.toml` and bundled in `:core:core-testing`. Add one line to any module's `build.gradle.kts`:

```kotlin
dependencies {
    testImplementation(project(":core:core-testing"))
}
```

This transitively provides `MainDispatcherRule`, `TestAppDispatchers`, JUnit 4, MockK, and Turbine.

## `MainDispatcherRule` and `TestAppDispatchers`

Both live in `:core:core-testing` — no need to recreate them.

```kotlin
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() { ... }

// Factory — pass a custom dispatcher to control timing in tests
fun TestAppDispatchers(dispatcher: TestDispatcher = UnconfinedTestDispatcher()): AppDispatchers
```

`UnconfinedTestDispatcher` and `Dispatchers.setMain` / `resetMain` are `@ExperimentalCoroutinesApi`. Any test class that uses `MainDispatcherRule` or `TestAppDispatchers` directly must opt in:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class <Name>ViewModelTest { ... }
```

## Known build gotcha — JUnit5 packaging conflict

If a module's `build.gradle.kts` pulls in JUnit5 transitively (via `:core:core-testing`), the Android build will fail with a duplicate `META-INF/LICENSE.md` error. Add this block to the affected module's `build.gradle.kts`:

```kotlin
android {
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}
```

`:core:core-testing` already has this block, so it only needs adding to modules that pull in JUnit5 via other means.

## Unit testing a ViewModel

```kotlin
class <Name>ViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val fakeUseCase = mockk<Get<Name>sUseCase>()
    private lateinit var viewModel: <Name>ViewModel

    @Before fun setUp() {
        viewModel = <Name>ViewModel(fakeUseCase)
    }

    @Test fun `loading state transitions to success`() = runTest {
        every { fakeUseCase(Unit) } returns flowOf(Result.Success(emptyList()))

        viewModel.onAction(<Name>Action.LoadContent)

        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isFalse()
        }
    }
}
```

Key rules:
- Always inject `AppDispatchers` in the ViewModel under test — replace with `TestDispatcher` via `MainDispatcherRule`.
- Use `Turbine` (`.test { }`) to assert on `StateFlow` / `Flow` emissions.
- Never expose `MutableStateFlow` — test through `uiState`.

## Integration testing Room (instrumented)

```kotlin
@RunWith(AndroidJUnit4::class)
class <Name>DaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: <Name>Dao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.<name>Dao()
    }

    @After fun tearDown() = db.close()

    @Test fun insertAndRetrieve() = runTest {
        dao.insertAll(listOf(testEntity()))
        dao.getAll().first().also { assertThat(it).hasSize(1) }
    }
}
```

## Compose UI test with Hilt

```kotlin
@HiltAndroidTest
class <Name>ScreenTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Before fun setUp() = hiltRule.inject()

    @Test fun screenDisplaysContent() {
        composeRule.onNodeWithText("Expected Text").assertIsDisplayed()
    }
}
```

## Commands

```bash
./gradlew :app:testDevDebugUnitTest             # app unit tests (dev flavor required)
./gradlew :<module>:testDebugUnitTest           # single non-app module unit tests (no flavor needed)
./gradlew :app:connectedDevDebugAndroidTest     # instrumented tests (device/emulator required)
```
