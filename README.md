# WSIW — What Should I Watch?

A cinematic movie discovery app for Android, powered by the [TMDB API](https://www.themoviedb.org/documentation/api). Browse trending films, search by title, read details, and build a personal watchlist — all with an offline-first architecture and a dark, cinema-inspired UI.

---

## Features

- **Discover** — Paginated grid of popular movies with poster art, title, and rating
- **Search** — Debounced full-text search with shimmer loading and empty states
- **Detail** — Collapsing backdrop, runtime, genres, overview, and dynamic accent color extracted from the poster
- **Watchlist** — Room-backed local list with add/remove, empty state, and snackbar feedback
- **Offline-first** — Stale-while-revalidate caching with a 10-minute TTL; pull-to-refresh forces a network fetch
- **Navigation** — Jetpack Navigation3 with custom transitions: slide for detail push/pop, crossfade for tab switching
- **Animated splash screen** — Letterbox curtain: two cinema-black bars slide in from top and bottom, the home screen peeks through the gap, then the bars retract to reveal the app (~1.2 s total)

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation3 (`NavDisplay`, `NavKey`) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Networking | Retrofit + OkHttp |
| Images | Coil |
| Local storage | Room |
| Architecture | MVVM + Clean Architecture (UseCase → Repository → DataSource) |

---

## Project Structure

```
WSIW/
├── app/                    # Application module — NavDisplay, Scaffold, bottom nav, splash animation
├── build-logic/            # Convention plugins (AGP, Compose, Hilt, etc.)
└── core/
│   ├── core-common/        # Base UseCase, Result, SafeApiCall
│   ├── core-data/          # Repositories, Room entities, Retrofit DTOs
│   ├── core-datastore/     # DataStore preferences
│   ├── core-domain/        # Domain models, repository interfaces
│   ├── core-network/       # OkHttp/Retrofit setup, auth interceptor
│   ├── core-testing/       # Shared test utilities — MainDispatcherRule, fakeMovie(), fakeMovieDetail()
│   └── core-ui/            # Theme, spacing tokens, shared components
└── feature/
    ├── feature-detail/     # Movie detail screen
    ├── feature-home/       # Discover screen
    ├── feature-search/     # Search screen
    └── feature-watchlist/  # Watchlist screen
```

---

## Setup

### 1. TMDB API key

This project uses the TMDB **Read Access Token** (the long JWT, not the short API key).

1. Create an account at [themoviedb.org](https://www.themoviedb.org) and generate a token under **Settings → API**
2. Add it to `local.properties` in the project root (this file is git-ignored and never committed):

```properties
tmdb.access.token=eyJhbGciOiJIUzI1NiJ9...
```

The token is injected at build time via `BuildConfig.TMDB_ACCESS_TOKEN` — it never touches source control.

### 2. Build

```bash
./gradlew assembleDevDebug
```

Two product flavors are configured — `dev` and `prod` — both currently point at the same TMDB endpoint. The `dev` variant appends `.dev` to the application ID so both can be installed side-by-side.

---

## Testing

```bash
./gradlew testDebugUnitTest
```

ViewModel unit tests cover all four feature modules (27 tests total). The test stack is JUnit 4 + MockK + Turbine + `kotlinx-coroutines-test`. Each test class uses a `MainDispatcherRule` that replaces `Dispatchers.Main` with `UnconfinedTestDispatcher`, so coroutines run eagerly and state settles synchronously after each action — no manual `advanceUntilIdle()` needed except in `SearchViewModelTest`, where `advanceTimeBy(400)` is used to fire the 300 ms debounce.

| Test class | What's covered |
|---|---|
| `HomeViewModelTest` | Success/error paths, forceRefresh flag on pull-to-refresh, retry call count, shimmer stays visible on empty cache |
| `SearchViewModelTest` | Immediate result clearing on query change, blank-query guard, debounce timing, success and error states |
| `WatchlistViewModelTest` | List population, `RemoveMovie` args and snackbar event, unknown-id no-op guard |
| `DetailViewModelTest` | Load success/error, error-with-cache sends event instead of overwriting screen, `isWatchlisted` flow, `ToggleWatchlist` passes correct flag |

`DetailViewModelTest` uses `fakeMovieDetail(posterUrl = null, backdropUrl = null)` so the Coil-based palette extraction path returns early and the `Context` mock is never touched.

---

## Architecture Notes

- **Offline-first**: `NetworkBoundResource` emits cached data immediately, then fetches fresh data and re-emits. A 10-minute TTL skips the network if the cache is recent enough.
- **Single source of truth**: Room is the source of truth for all lists; the network layer only writes to the database, never directly to the UI.
- **Clean separation**: feature modules depend only on `core-domain` interfaces — no feature module touches Retrofit or Room directly.
- **TMDB token security**: the token lives in `local.properties` (git-ignored) and is surfaced to the app exclusively through `BuildConfig`. It is never hardcoded in source files.
