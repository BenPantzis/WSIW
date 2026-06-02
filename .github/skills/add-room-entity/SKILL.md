---
name: add-room-entity
description: Add a Room entity and DAO to core-database, register in AppDatabase, bind via DatabaseModule, and expose through a repository interface in core-domain implemented in core-data.
metadata:
  author: com.template.android
  last-updated: '2026-05-23'
  keywords: [Room, database, entity, DAO, repository, offline-first, Hilt, core-database, core-data, core-domain]
---

## Overview

All Room code lives in `:core:core-database`. Repository interfaces live in `:core:core-domain` (pure Kotlin — no Room imports). Implementations live in `:core:core-data`.

## Hard constraints

- `@Entity` and `@Dao` classes must NOT be placed outside `:core:core-database`.
- `:core:core-domain` must have no Android framework imports — expose domain models, not Room entities.

## Steps

### 1. Create `@Entity` in `:core:core-database`

Path: `core/core-database/src/main/java/com/template/android/core/database/entity/<Name>Entity.kt`

```kotlin
@Entity(tableName = "<name>s")
data class <Name>Entity(
    @PrimaryKey val id: String,
    val fieldOne: String,
    val fieldTwo: Int,
)
```

### 2. Create `@Dao` in `:core:core-database`

Path: `core/core-database/src/main/java/com/template/android/core/database/dao/<Name>Dao.kt`

```kotlin
@Dao
interface <Name>Dao {
    @Query("SELECT * FROM <name>s")
    fun getAll(): Flow<List<<Name>Entity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<<Name>Entity>)

    @Delete
    suspend fun delete(item: <Name>Entity)
}
```

### 3. Register entity in `AppDatabase.kt`

```kotlin
@Database(
    entities = [
        ...,
        <Name>Entity::class,   // add here
    ],
    version = <increment version>,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun <name>Dao(): <Name>Dao   // add abstract accessor
}
```

Add a `Migration` for production. For development only, `fallbackToDestructiveMigration(true)` is acceptable.

### 4. Expose DAO in `DatabaseModule.kt`

```kotlin
@Provides
fun provide<Name>Dao(db: AppDatabase): <Name>Dao = db.<name>Dao()
```

### 5. Define repository interface in `:core:core-domain`

Path: `core/core-domain/src/main/java/com/template/android/core/domain/repository/<Name>Repository.kt`

```kotlin
interface <Name>Repository {
    fun getAll(): Flow<List<<Name>Model>>
    suspend fun sync()
}
```

Use a plain domain model class (`<Name>Model`), not `<Name>Entity`.

### 6. Implement repository in `:core:core-data`

Path: `core/core-data/src/main/java/com/template/android/core/data/repository/<Name>RepositoryImpl.kt`

```kotlin
class <Name>RepositoryImpl @Inject constructor(
    private val dao: <Name>Dao,
) : <Name>Repository {
    override fun getAll(): Flow<List<<Name>Model>> =
        dao.getAll().map { it.map(<Name>Entity::toDomainModel) }

    override suspend fun sync() { /* fetch from network, insert via dao */ }
}
```

Bind with a Hilt module (use `@Binds`, not `@Provides`):

```kotlin
@Binds
abstract fun bind<Name>Repository(impl: <Name>RepositoryImpl): <Name>Repository
```

## Verification

```bash
./gradlew :core:core-database:assembleDebug
./gradlew :core:core-data:assembleDebug
```
