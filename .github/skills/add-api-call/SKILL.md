---
name: add-api-call
description: Add a Retrofit API endpoint end-to-end — service interface in core-network, offline-first repository in core-data using networkBoundResource, UseCase in core-domain, injected into a feature ViewModel.
metadata:
  author: com.template.android
  last-updated: '2026-05-23'
  keywords: [Retrofit, network, API, offline-first, networkBoundResource, UseCase, Hilt, core-network, core-data, core-domain]
---

## Overview

Network services live in `:core:core-network`. Repositories in `:core:core-data` combine the service and a Room DAO using `networkBoundResource` for offline-first flows. Use cases in `:core:core-domain` expose data to feature ViewModels. Feature modules never import `:core:core-network` or `:core:core-data` directly.

## Hard constraints

- Retrofit service interfaces must NOT be placed outside `:core:core-network`.
- Feature modules inject `UseCase` subclasses, never repositories or services directly.

## Steps

### 1. Define Retrofit service in `:core:core-network`

Path: `core/core-network/src/main/java/com/template/android/core/network/api/<Name>ApiService.kt`

```kotlin
interface <Name>ApiService {
    @GET("<endpoint>")
    suspend fun get<Name>s(): List<<Name>ApiModel>
}
```

Add a data class for the JSON payload (`<Name>ApiModel`) in the same package. Use `@SerialName` if using `kotlinx.serialization`.

### 2. Bind service in `NetworkModule.kt`

```kotlin
@Provides
@Singleton
fun provide<Name>ApiService(retrofit: Retrofit): <Name>ApiService =
    retrofit.create(<Name>ApiService::class.java)
```

### 3. Create the repository in `:core:core-data`

Inject both the API service and the DAO, then use `networkBoundResource`:

```kotlin
class <Name>RepositoryImpl @Inject constructor(
    private val api: <Name>ApiService,
    private val dao: <Name>Dao,
) : <Name>Repository {
    override fun get<Name>s(): Flow<Result<List<<Name>Model>>> =
        networkBoundResource(
            query = { dao.getAll().map { it.map(<Name>Entity::toDomainModel) } },
            fetch = { api.get<Name>s() },
            saveFetchResult = { items -> dao.insertAll(items.map { it.toEntity() }) },
        )
}
```

`networkBoundResource` is in `core/core-data/src/main/java/.../util/NetworkBoundResource.kt`.

### 4. Define a `FlowUseCase` in `:core:core-domain`

Path: `core/core-domain/src/main/java/com/template/android/core/domain/usecase/Get<Name>sUseCase.kt`

```kotlin
class Get<Name>sUseCase @Inject constructor(
    private val repository: <Name>Repository,
    dispatchers: AppDispatchers,
) : FlowUseCase<Unit, List<<Name>Model>>(dispatchers) {
    override fun execute(params: Unit): Flow<Result<List<<Name>Model>>> =
        repository.get<Name>s()
}
```

Use `FlowUseCase` (not `UseCase`) whenever the repository returns a `Flow`. `FlowUseCase` applies `flowOn(dispatchers.io)` to the upstream, which correctly covers both the Room query and any network calls inside `networkBoundResource`.

### 5. Inject the UseCase into the ViewModel

```kotlin
@HiltViewModel
class <Name>ViewModel @Inject constructor(
    private val get<Name>s: Get<Name>sUseCase,
) : BaseViewModel<<Name>Action, <Name>Event, <Name>UiState>(
    initialState = <Name>UiState(),
) {
    override fun handleAction(action: <Name>Action) {
        when (action) {
            <Name>Action.LoadContent -> loadContent()
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            get<Name>s(Unit).collect { result ->
                when (result) {
                    is Result.Loading -> updateState { copy(isLoading = true) }
                    is Result.Success -> updateState { copy(isLoading = false, items = result.data) }
                    is Result.Error -> sendEvent(<Name>Event.ShowSnackbar("Failed to load"))
                }
            }
        }
    }
}
```

## Verification

```bash
./gradlew :core:core-network:assembleDebug
./gradlew :core:core-data:assembleDebug
./gradlew :core:core-domain:assembleDebug
```
