# WSIW — Project Conventions

Reference for all development on this project. Every rule here is derived from the actual codebase — follow them exactly so new code is indistinguishable from existing code.

---

## Module structure

```
:app                    Application shell — DI wiring, navigation host, splash
:core:core-common       Shared primitives: Result, AppDispatchers
:core:core-network      Retrofit / OkHttp setup
:core:core-database     Room DB, DAOs, entities, TypeConverters
:core:core-datastore    DataStore wrappers (token provider, session, preferences)
:core:core-ui           BaseViewModel, UiText, theme, spacing, common composables
:core:core-testing      MainDispatcherRule, shared test fixtures
:layer:layer-domain     Repository interfaces, use case base classes, domain models
:layer:layer-data       Repository implementations, DTOs, mappers, Hilt modules
:feature:feature-*      One module per screen group
```

**Layering rules (strict):**
- Feature modules depend only on `:layer:layer-domain` and `:core:core-ui` — never on `:layer:layer-data` or other features.
- `:layer:layer-data` depends on `:layer:layer-domain` and core modules.
- Core modules have no upward dependencies on features or layers.

---

## Build & security

**Flavors:** `dev` (appId suffix `.dev`) and `prod`. Both share the same `BASE_URL`.

**TMDB token:**
- Stored in `local.properties` as `tmdb.access.token` — this file is in `.gitignore` and must never be committed.
- Injected as `BuildConfig.TMDB_ACCESS_TOKEN` at build time via `buildConfigField` in `app/build.gradle.kts`.
- **Never hardcode the token value in any source file.** Reference it only through `BuildConfig.TMDB_ACCESS_TOKEN`.

---

## Architecture: MVI with BaseViewModel

Every ViewModel extends `BaseViewModel<Action, Event, UiState>` from `:core:core-ui`:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(...) :
    BaseViewModel<MyAction, MyEvent, MyUiState>(initialState = MyUiState()) {

    override fun handleAction(action: MyAction) {
        when (action) { ... }
    }
}
```

| Mechanism | How |
|---|---|
| Update state | `updateState { copy(field = newValue) }` |
| Emit a one-shot event | `sendEvent(MyEvent.NavigateBack)` |
| Dispatch from UI | `viewModel.onAction(MyAction.Foo)` |
| Observe state | `val state by viewModel.uiState.collectAsStateWithLifecycle()` |
| Collect events | `LaunchedEffect(Unit) { viewModel.events.collect { ... } }` |

### Contract files

Each feature has a `MyContract.kt` (or `MyContract`) with three declarations — always `sealed interface` for actions and events, `data class` for state:

```kotlin
sealed interface MyAction {
    data object Refresh : MyAction
    data class Select(val id: Int) : MyAction
}

sealed interface MyEvent {
    data class ShowSnackbar(val message: UiText) : MyEvent
}

data class MyUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: UiText? = null,
)
```

### UiText

ViewModels never hold raw strings. Use `UiText`:

```kotlin
// In ViewModel:
updateState { copy(error = UiText.StringResource(R.string.my_error)) }
sendEvent(MyEvent.ShowSnackbar(UiText.Plain(e.message ?: "Unknown error")))

// In Composable:
Text(state.error.asString())
```

`UiText.StringResource` — safe for ViewModels (no Context).
`UiText.Plain` — only for raw strings from external sources (network errors, etc.).

---

## Navigation (AndroidX Navigation 3)

Nav keys are `@Serializable` data objects or data classes implementing `NavKey`:

```kotlin
@Serializable data object HomeKey : NavKey

@Serializable data class DetailKey(val movieId: Int) : NavKey
```

Each feature exposes an extension on `EntryProviderScope<NavKey>`:

```kotlin
fun EntryProviderScope<NavKey>.homeDestination(onMovieClick: (Int) -> Unit) {
    entry<HomeKey> { HomeScreen(onMovieClick = onMovieClick) }
}
```

Navigation uses a `mutableStateListOf<NavKey>` back stack directly — no `NavController`. Move forward with `backStack.add(key)`, back with `backStack.removeLastOrNull()`, tab-switch with `backStack.clear(); backStack.add(key)`.

---

## Dependency injection (Hilt)

All modules install in `SingletonComponent`. Pattern: `abstract class` with `@Binds` for interfaces, `companion object` with `@Provides` for constructed objects.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MyModule {

    @Binds @Singleton
    abstract fun bindMyRepository(impl: MyRepositoryImpl): MyRepository

    companion object {
        @Provides @Singleton
        fun provideApiService(retrofit: Retrofit): MyApiService =
            retrofit.create(MyApiService::class.java)
    }
}
```

---

## Repository & use case pattern

**Interfaces** live in `:layer:layer-domain` under `core.domain.repository`.
**Implementations** live in `:layer:layer-data` under `core.data.<domain>`.

Use cases extend one of two base classes from `:layer:layer-domain`:

```kotlin
// Streaming result
class GetMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<NoParams, List<Movie>>(dispatchers) {
    override fun execute(params: NoParams): Flow<Result<List<Movie>>> =
        repository.getMovies()
}

// One-shot suspend result
class GetFavoriteCountUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : UseCase<Int, Int>(dispatchers) {
    override suspend fun execute(params: Int): Int =
        repository.getFavoriteCount(accountId = params)
}
```

Use `NoParams` (from `:layer:layer-domain`) when the use case takes no arguments.

---

## DTOs and mappers

DTOs are pure data holders using `@SerializedName` (Gson). They live in `:layer:layer-data` under `core.data.remote.model`.

Mappers are **extension functions in a dedicated mapper file** in the same package as the repository impl — never inside the DTO or entity file.

```kotlin
// core/data/movie/MovieMapper.kt
private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

fun MovieDto.toDomain() = Movie(
    id = id,
    posterUrl = posterPath?.let { "$POSTER_BASE_URL$it" },
    // ...
)

fun MovieDto.toEntity(page: Int) = PopularMovieEntity(...)

fun PopularMovieEntity.toDomain() = Movie(...)
```

Domain models store **full URLs** — path construction happens at the mapper boundary, never in the UI.

---

## Theme system

The app is **dark-only**. There is no light color scheme. Never add a light theme variant.

```kotlin
WSIWTheme { ... }   // always wraps the app
```

**Access tokens through MaterialTheme or AppTheme — never hardcode values:**

```kotlin
// Colors
MaterialTheme.colorScheme.primary          // GoldDefault #E8A020
MaterialTheme.colorScheme.background       // CinemaBlack #0D0D0D
MaterialTheme.colorScheme.surface          // CinemaDark #161618
MaterialTheme.colorScheme.surfaceVariant   // CinemaSurface #1C1C1E

// Spacing — always AppTheme.spacing, never raw dp
AppTheme.spacing.xs       // 4.dp
AppTheme.spacing.sm       // 8.dp
AppTheme.spacing.md       // 12.dp
AppTheme.spacing.lg       // 16.dp
AppTheme.spacing.content  // 20.dp
AppTheme.spacing.xl       // 24.dp
AppTheme.spacing.xxl      // 32.dp

// Typography
MaterialTheme.typography.titleLarge
MaterialTheme.typography.bodyMedium
// etc.

// Shapes (4/8/12/16/24 dp for extraSmall–extraLarge)
MaterialTheme.shapes.small
MaterialTheme.shapes.medium
```

---

## Common composables (core-ui)

Always prefer these over one-off implementations:

| Composable | Use for |
|---|---|
| `MoviePosterCard(posterUrl, title, voteAverage, onClick, modifier, overlayContent)` | Any movie poster with vote badge; `overlayContent` slot for badges |
| `AvatarImage(url, name, size, modifier, textStyle)` | Circular avatar with initial fallback |
| `RemoteImage(url, contentDescription, modifier, contentScale)` | Any async remote image (Coil) |
| `ScreenScaffold(modifier, snackbarHostState, topBar, floatingActionButton, content)` | Standard screen wrapper |
| `EmptyState(icon, title, body, modifier, action)` | Zero-data states; `icon` is an emoji string |
| `ErrorContent(message, modifier, onRetry)` | Error states with optional retry |
| `ShimmerPosterCard` / `Shimmer` | Loading skeletons |

---

## Strings

**All user-visible strings must be in `strings.xml`.** Never hardcode visible text in Kotlin source files.

- Each feature module has its own `src/main/res/values/strings.xml`.
- Shared strings (retry labels, etc.) live in `core-ui/src/main/res/values/strings.xml`.
- In composables: `stringResource(R.string.my_string)`.

**Naming convention:** `<module>_<component_or_semantic>_<qualifier>` — all lowercase snake_case.

```xml
<string name="profile_stats_watchlist">Watchlist</string>
<string name="profile_stats_rated">Rated</string>
<string name="profile_cd_sign_out">Sign out</string>        <!-- cd_ prefix for content descriptions -->
<string name="profile_ratings_header">Your Ratings</string>
<string name="profile_auth_subtitle">Signed in via TMDB</string>
<string name="profile_unauthenticated_body">Connect your TMDB account to rate films and sync your watchlist.</string>
```

---

## Testing

**Libraries:** JUnit 4, MockK, kotlinx-coroutines-test, Turbine, `MainDispatcherRule` from `:core:core-testing`.

**Coroutine setup (ViewModel tests):**

```kotlin
private val testDispatcher = UnconfinedTestDispatcher()

@get:Rule
val mainDispatcherRule = MainDispatcherRule(testDispatcher)
```

**Test names:** Use backtick-quoted descriptive sentences:
```kotlin
@Test
fun `loading state is emitted before results arrive`() { ... }
```

**DAO / Room tests:** Use Robolectric with in-memory DB. Pin SDK in `src/test/resources/robolectric.properties`:
```
sdk=34
```
Obtain the application context via `RuntimeEnvironment.getApplication()`.

---

## Kotlin style

- **Trailing commas** on every multi-line parameter list and argument list — always.
- **American English** spelling for all identifiers and strings (`favoriteCount`, not `favouriteCount`; `color`, not `colour`).
- **No comments** unless the *why* is non-obvious (a hidden constraint, a workaround, a subtle invariant). Never comment what the code does.
- Detekt is configured with `maxIssues: 0`. Run `./gradlew detekt` before committing.

---

## Git commits

- Imperative mood, present tense: "Add rating distribution chart", not "Added" or "Adding".
- Subject line only — no body paragraphs unless truly necessary.
- No ticket/issue numbers.
- No `Co-Authored-By` lines.

---

## What not to do

- **Don't** import or depend on `:layer:layer-data` from a feature module.
- **Don't** hardcode any dp values — use `AppTheme.spacing.*`.
- **Don't** hardcode any color values — use `MaterialTheme.colorScheme.*`.
- **Don't** hardcode any visible string — use `stringResource()`.
- **Don't** commit `local.properties` or any file containing the TMDB access token.
- **Don't** add a light theme — the app is intentionally dark-only.
- **Don't** create a new composable that duplicates `EmptyState`, `ErrorContent`, `MoviePosterCard`, etc.
- **Don't** put mapper logic inside DTO or entity files — use a dedicated mapper file.
- **Don't** hold raw strings in ViewModels — use `UiText`.
