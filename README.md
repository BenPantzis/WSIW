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
├── app/                    # Application module — NavDisplay, Scaffold, bottom nav
├── build-logic/            # Convention plugins (AGP, Compose, Hilt, etc.)
└── core/
│   ├── core-common/        # Base UseCase, Result, SafeApiCall
│   ├── core-data/          # Repositories, Room entities, Retrofit DTOs
│   ├── core-datastore/     # DataStore preferences
│   ├── core-domain/        # Domain models, repository interfaces
│   ├── core-network/       # OkHttp/Retrofit setup, auth interceptor
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

## Architecture Notes

- **Offline-first**: `NetworkBoundResource` emits cached data immediately, then fetches fresh data and re-emits. A 10-minute TTL skips the network if the cache is recent enough.
- **Single source of truth**: Room is the source of truth for all lists; the network layer only writes to the database, never directly to the UI.
- **Clean separation**: feature modules depend only on `core-domain` interfaces — no feature module touches Retrofit or Room directly.
- **TMDB token security**: the token lives in `local.properties` (git-ignored) and is surfaced to the app exclusively through `BuildConfig`. It is never hardcoded in source files.
