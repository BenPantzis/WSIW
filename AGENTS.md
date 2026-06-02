# AGENTS.md

Cross-tool conventions for AI agents working in this repository.
Full architecture rules are in [CLAUDE.md](./CLAUDE.md).

## Project snapshot

Multi-module Android app · MVVM + Clean Architecture  
Package root: `com.template.android` · Min SDK: 24 · Compile SDK: 36  
Build: Gradle 9.4.1 · AGP 9.2.1 · Kotlin 2.2.10  
UI: Jetpack Compose · DI: Hilt · DB: Room · Network: Retrofit · Async: Coroutines + Flow

## Module boundaries (never break these)

```
app  →  :feature:feature-*  →  :core:core-ui, :core:core-common
                              ↕  (no cross-feature deps)

:core:core-data  →  :core:core-domain, :core:core-network, :core:core-database
:core:core-domain  →  :core:core-common  (pure Kotlin — no Android imports)
```

- Features never import `:core:core-data`, `:core:core-network`, or `:core:core-database`.
- `:core:core-domain` has zero Android framework imports.
- `:app` only wires navigation and `MyApplication`; no business logic.

## Key rules

- Kotlin only. Compose only (no XML layouts). `StateFlow` only (no `LiveData`).
- All feature ViewModels extend `BaseViewModel<Action, Event, UiState>` from `:core:core-ui`.
- `UiState` is always a `data class` with defaults — never a sealed interface.
- All versions live in `gradle/libs.versions.toml`. Never hardcode a version in a build file.
- Do NOT apply `org.jetbrains.kotlin.android` manually — AGP 9.x applies it automatically.
- `@Entity` / `@Dao` classes belong only in `:core:core-database`.
- Retrofit service interfaces belong only in `:core:core-network`.

## Build commands

```bash
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :<module>:testDebugUnitTest
```

## Available skills

Load the relevant skill file before starting the corresponding task:

| Task | Skill file |
|---|---|
| Add a new feature screen/module | [`.github/skills/add-feature-module/SKILL.md`](.github/skills/add-feature-module/SKILL.md) |
| Add a Room entity + DAO + repository | [`.github/skills/add-room-entity/SKILL.md`](.github/skills/add-room-entity/SKILL.md) |
| Add a Retrofit API call end-to-end | [`.github/skills/add-api-call/SKILL.md`](.github/skills/add-api-call/SKILL.md) |
| Add a library dependency | [`.github/skills/add-dependency/SKILL.md`](.github/skills/add-dependency/SKILL.md) |
| Write or set up tests | [`.github/skills/android-testing/SKILL.md`](.github/skills/android-testing/SKILL.md) |
