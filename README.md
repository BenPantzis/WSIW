# WSIW — What Should I Watch?

A full-featured movie discovery and tracking app for Android, built as a portfolio project to demonstrate production-quality Android engineering. Powered by the [TMDB API](https://www.themoviedb.org/documentation/api).

<br>

## What it does

- **Discover** — Paginated movie grid with category tabs (Trending, Top Rated, Now Playing, Upcoming), genre filtering, and a filter sheet for sort order, minimum rating, and release year — all routed through `/discover/movie`
- **TV Shows** — Paginated TV grid with the same category/genre/filter system as Discover, plus a full detail screen (seasons, cast, content rating, trailer, similar shows) powered by `/discover/tv` and `/tv/{id}`
- **Search** — Debounced multi-search (`/search/multi`) returning a mixed grid of movies, TV shows, and people with a dedicated people section; trending idle state and shimmer skeletons
- **Detail** — Collapsing backdrop, trailer, cast, similar titles, recommendations, watch providers (streaming/rent/buy), reviews, content rating, and dynamic accent color extracted from the poster palette
- **Watchlist** — Room-backed personal list with swipe-to-dismiss, undo, sort options, and grid/list toggle persisted across sessions
- **Ratings** — TMDB-synced personal ratings with a custom drag-to-rate bottom sheet and emoji feedback animations
- **Profile** — TMDB OAuth sign-in (v4 browser handoff flow) with account info and sign-out; signed-in state gates write operations
- **Offline-first** — Stale-while-revalidate caching with a 10-minute TTL; movies and movie detail serve instantly from Room while a background fetch runs (TV, search, and genres fetch on every load)

<br>

## Architecture

This project applies Clean Architecture with a strict, enforced module hierarchy. Dependency direction is one-way and verified at every layer.

```
feature-* → layer-domain ← layer-data ← core-*
```

**The rules, held strictly:**
- `core-*` modules are pure infrastructure — they have zero upward dependencies on `layer-*` or `feature-*`
- `feature-*` modules depend only on `layer-domain` interfaces, `core-ui`, and `core-common` — never on Room, DataStore, Retrofit, or any `layer-data` impl
- `layer-data` is the only module allowed to depend on all infrastructure layers at once; it wires repository interfaces to their implementations via Hilt bindings
- `layer-domain` has no app-module dependencies at all — domain models and repository interfaces are stable, framework-free Kotlin

This means: a feature ViewModel never imports a Room DAO, a Retrofit service, or a DataStore key. It calls a use case. The use case calls a repository interface. The implementation lives elsewhere.

### Module map

```
WSIW/
├── app/                        # Composition root — NavDisplay, bottom nav, Hilt entry point, splash
├── build-logic/
│   └── convention/             # Custom Gradle convention plugins (AGP, Compose, Hilt, KSP)
├── core/
│   ├── core-common/            # Result<T>, base UseCase/FlowUseCase, SafeApiCall
│   ├── core-database/          # Room database, DAOs, entities, TypeConverters
│   │                             (no domain imports — internal *Data mirror types for entity fields)
│   ├── core-datastore/         # DataStore<Preferences> setup and PreferencesRepository wrapper
│   ├── core-network/           # OkHttp/Retrofit factory, AuthInterceptor, token provider interface
│   ├── core-testing/           # MainDispatcherRule, fakeMovie(), fakeMovieDetail() shared fakes
│   └── core-ui/                # Material3 theme, spacing tokens, shared Composables
├── layer/
│   ├── layer-domain/           # Domain models, repository interfaces, use cases
│   │                             (pure Kotlin — no Android, no framework)
│   └── layer-data/             # Repository implementations, DTO↔entity mappers, Hilt bindings
│                                 (the only module that sees both domain interfaces and infrastructure)
└── feature/
    ├── feature-detail/         # Movie detail screen, rating sheet, person screen, reviews
    ├── feature-home/           # Discover grid, genre/category filtering
    ├── feature-profile/        # TMDB OAuth sign-in, account view, sign-out
    ├── feature-search/         # Debounced multi-search (movies + TV + people), trending idle state
    ├── feature-tv/             # TV show list and detail screens
    └── feature-watchlist/      # Watchlist grid/list, swipe-to-dismiss, sort, view-mode persistence
```

### Key patterns

**NetworkBoundResource** — all list screens emit cached data first, fetch in the background, and re-emit. A TTL check gates whether the network call fires. This gives instant display and silent freshness without pull-to-refresh being mandatory.

**Single source of truth** — Room is the source of truth for popular movies, movie detail, watchlist, and ratings. The network layer only writes to the database; it never emits directly to the UI.

**Use cases as the domain boundary** — ViewModels take use cases as constructor parameters, not repositories. This keeps the test surface small: swapping a fake use case is trivial; faking a full repository is not.

**Hilt throughout** — every ViewModel, repository, and singleton is injected. Abstract `@Binds` modules in `layer-data` tie each interface to its implementation. The `app` module is the only place where infrastructure wiring (`@Provides` for Room, Retrofit, DataStore) lives outside its own module.

**Convention plugins** — `build-logic/convention` defines `template.android.library`, `template.android.compose`, and `template.android.hilt`. Adding a module is three plugin lines and a namespace declaration. Dependency versions are in a single version catalog.

<br>

## Tech stack

| Concern | Library / API |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation3 (`NavDisplay`, `NavKey`, `BackStack`) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Networking | Retrofit 2 + OkHttp 4 |
| Local storage | Room |
| Preferences | DataStore |
| Images | Coil |
| Auth | TMDB API v4 (OAuth browser handoff + deep-link callback) |
| Build | Gradle convention plugins, version catalog, KSP |

<br>

## Design system

The UI is built on a small, consistent token set rather than ad-hoc magic values.

- **Spacing** — `xs 4dp · sm 8dp · md 12dp · lg 16dp · content 20dp · xl 24dp · xxl 32dp`, accessed via `AppTheme.spacing` anywhere in the tree
- **Color** — Cinema-black surfaces (`#0A0A0A`, `#141414`) with a warm gold accent (`#E8A020`). Dynamic accent color is extracted per-movie from the poster palette using `androidx.palette` and propagated down the detail screen via a `LocalAccentColor` CompositionLocal
- **Animation** — Rating feedback uses Compose `Animatable` with a `spring(dampingRatio=0.5, stiffness=350)` for the emoji pop, an 8-dot particle ring drawn on a full-screen `Canvas`, and `graphicsLayer` reads so every frame is a GPU layer redraw with no recomposition

<br>

## TMDB authentication

The app supports full TMDB user sign-in via the v4 API:

1. App POSTs to `/4/auth/request_token` using the static app token
2. User is sent to the TMDB approval page in a Custom Tab
3. TMDB redirects to `wsiw://auth/callback?request_token={token}`
4. `MainActivity.onNewIntent` catches the deep link and pushes the callback destination onto the nav stack
5. App exchanges the approved request token for a user access token via `/4/auth/access_token`
6. The user's access token is persisted in DataStore and used for all subsequent write requests (ratings, watchlist sync)

Signed-out reads continue to use the static app token from `BuildConfig`. The token is never hardcoded in source — it lives in `local.properties` (git-ignored) and is injected at build time.

<br>

## Setup

### 1. TMDB Read Access Token

Generate a token at [themoviedb.org](https://www.themoviedb.org) under **Settings → API** (the long JWT, not the short API key). Add it to `local.properties` in the project root:

```properties
tmdb.access.token=eyJhbGciOiJIUzI1NiJ9...
```

This file is git-ignored and never committed.

### 2. Build

```bash
./gradlew assembleDevDebug
```

Two product flavors are configured — `dev` and `prod` — both pointing at the same TMDB endpoint. The `dev` variant appends `.dev` to the application ID so both can coexist on a device.

<br>

## Testing

```bash
./gradlew testDebugUnitTest
```

Unit tests cover all eleven feature ViewModels, Room DAO integration tests for all four DAOs, plus screenshot tests for the Home screen. The stack is JUnit 4 + MockK + Turbine + `kotlinx-coroutines-test`, with a `MainDispatcherRule` that replaces `Dispatchers.Main` with `UnconfinedTestDispatcher` so coroutines settle synchronously after each action.

| Test class | Coverage |
|---|---|
| `HomeViewModelTest` | Success/error paths, error-with-cache fires snackbar and preserves movies, retry and refresh trigger a second use-case call, pagination appends movies and increments page, category switch resets list, retry clears previous error |
| `HomeScreenTest` | Roborazzi screenshot tests for loading skeleton and populated content states |
| `SearchViewModelTest` | Initial empty state, immediate stale-result clearing on query change, blank-query guard, 300 ms debounce timing, error state |
| `WatchlistViewModelTest` | Watchlist population, ratings map, view mode restored from DataStore on init, grid↔list toggle with persistence, sort selection, `RemoveMovie` use-case args and snackbar event, unknown-id no-op guard, `UndoRemove` re-adds the movie |
| `DetailViewModelTest` | Load success/error (with and without cached movie), retry triggers second load, `isWatchlisted` flow, `ToggleWatchlist` auth-gating and correct flag, `userRating` flow, `ShowRatingDialog` auth-gating, dismiss dialog, `RateMovie` fires event and calls use case, `RemoveRating` delegates to use-case remove |
| `TvListViewModelTest` | Success/error paths, error-with-cache fires snackbar and preserves shows, retry and refresh trigger a second use-case call, pagination appends shows, category switch resets list, retry clears previous error |
| `TvDetailViewModelTest` | Initial loading state, load success/error, same-id no-op guard, load with different id replaces show, retry triggers second fetch |
| `ProfileViewModelTest` | Session state reflected in UI, watchlist and ratings counts, `startSignIn` sends `OpenBrowser` event with correct URL, `startSignIn` failure sets error, `completeSignIn` clears pending token on success and sets error on failure, `cancelSignIn` clears token, `signOut` delegates to use case |
| `LoginCallbackViewModelTest` | Token exchange success emits `NavigateToProfile`, failure sets error state, retry re-exchanges and emits navigation event, retry clears error before exchange |
| `ReviewsViewModelTest` | Initial load success/error, retry triggers second fetch, load-next-page appends reviews, load-next-page no-op at last page, pagination error with cached reviews sends snackbar and preserves list, sort change does not reload |
| `PersonViewModelTest` | Load success/error, retry triggers second fetch, retry after error clears error and loads person |
| `RatedMoviesViewModelTest` | Empty list on init, rated movies reflected in state, single movie with correct rating |
| `WatchlistDaoTest` | Insert/replace, ordering by `addedAt` DESC, `isWatchlisted` Flow reactivity, `deleteById` row count, `deleteAll`, nullable URL round-trip, Flow emits on write |
| `PopularMovieCacheDaoTest` | `insertAll` then `getByPage`, page isolation, replace semantics, `deleteByPage` row count and cross-page safety, Flow emits on write |
| `MovieDetailCacheDaoTest` | Insert/replace, `getById` reactivity on delete, genre pipe-delimiter round-trip, cast JSON round-trip (including `null` `profileUrl`), `similarMovies` JSON round-trip (including `null` image URLs), nullable column fields |
| `RatingDaoTest` | Upsert, upsert-replace, `getRating` Flow reactivity on update and delete, `getAll`, `insertAll`, `deleteAll` |

**Note:** The DAO tests caught a production bug in `Converters.kt` — nullable String fields inside JSON arrays (`posterUrl`, `backdropUrl`, `profileUrl`) were being deserialized as the string `"null"` rather than `null` due to `optString` behaviour on `JSONObject.NULL`. Fixed alongside the test addition.

`DetailViewModelTest` uses a `@Before` stub pattern — all neutral defaults are set once; individual tests override only the stubs they need, keeping each test focused on a single behaviour. The `Context` mock is satisfied by `fakeMovieDetail(posterUrl = null)`, which causes the palette extraction to short-circuit before any real Coil call.
