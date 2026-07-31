package com.bsp.wsiw.core.database

import app.cash.turbine.test
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import com.bsp.wsiw.core.database.model.CastMemberData
import com.bsp.wsiw.core.database.model.GenreData
import com.bsp.wsiw.core.database.model.MovieData
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MovieDetailCacheDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = buildTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(id: Int = 1) = MovieDetailEntity(
        id = id,
        title = "Detail Movie $id",
        overview = "Overview",
        posterUrl = "/poster.jpg",
        backdropUrl = "/backdrop.jpg",
        releaseDate = "2024-06-15",
        voteAverage = 8.1,
        voteCount = 2000,
        tagline = "A tagline",
        genres = listOf(GenreData(28, "Action"), GenreData(12, "Adventure")),
        runtime = 120,
        originalLanguage = "en",
        cachedAt = 1_000L,
        trailerKey = "dQw4w9WgXcQ",
        trailerName = "Official Trailer",
        cast = listOf(
            CastMemberData(1, "Actor One", "Hero", "/actor1.jpg"),
            CastMemberData(2, "Actor Two", "Villain", null),
        ),
        similarMovies = listOf(
            MovieData(10, "Similar A", "Overview A", "/sa.jpg", null, "2023-01-01", 7.0, 100),
        ),
        recommendedMovies = emptyList(),
        certification = "PG-13",
    )

    @Test
    fun `insert then getById returns entity`() = runTest {
        db.movieDetailCacheDao().insert(entity())
        db.movieDetailCacheDao().getById(1).test {
            assertEquals(entity(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getById returns null before any insert`() = runTest {
        db.movieDetailCacheDao().getById(999).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert with same id replaces existing entry`() = runTest {
        db.movieDetailCacheDao().insert(entity(id = 1).copy(title = "Original"))
        db.movieDetailCacheDao().insert(entity(id = 1).copy(title = "Replaced"))
        db.movieDetailCacheDao().getById(1).test {
            assertEquals("Replaced", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteById returns 1 for existing entry`() {
        db.movieDetailCacheDao().insert(entity(id = 5))
        val rows = db.movieDetailCacheDao().deleteById(5)
        assertEquals(1, rows)
    }

    @Test
    fun `deleteById returns 0 for unknown id`() {
        val rows = db.movieDetailCacheDao().deleteById(999)
        assertEquals(0, rows)
    }

    @Test
    fun `getById emits null after deleteById`() = runTest {
        db.movieDetailCacheDao().insert(entity(id = 3))
        db.movieDetailCacheDao().getById(3).test {
            assertEquals(entity(id = 3), awaitItem())
            db.movieDetailCacheDao().deleteById(3)
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // TypeConverter round-trips

    @Test
    fun `genres survive pipe-delimited round-trip`() = runTest {
        val genres = listOf(GenreData(28, "Action"), GenreData(35, "Comedy"))
        db.movieDetailCacheDao().insert(entity().copy(genres = genres))
        db.movieDetailCacheDao().getById(1).test {
            assertEquals(genres, awaitItem()?.genres)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty genres list survives round-trip`() = runTest {
        db.movieDetailCacheDao().insert(entity().copy(genres = emptyList()))
        db.movieDetailCacheDao().getById(1).test {
            assertEquals(emptyList<GenreData>(), awaitItem()?.genres)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cast survives JSON round-trip including nullable profileUrl`() = runTest {
        val cast = listOf(
            CastMemberData(1, "Name With Photo", "Role", "/photo.jpg"),
            CastMemberData(2, "Name No Photo", "Other Role", null),
        )
        db.movieDetailCacheDao().insert(entity().copy(cast = cast))
        db.movieDetailCacheDao().getById(1).test {
            assertEquals(cast, awaitItem()?.cast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty cast list survives round-trip`() = runTest {
        db.movieDetailCacheDao().insert(entity().copy(cast = emptyList()))
        db.movieDetailCacheDao().getById(1).test {
            assertEquals(emptyList<CastMemberData>(), awaitItem()?.cast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `similar movies survive JSON round-trip including nullable image urls`() = runTest {
        val movies = listOf(
            MovieData(10, "Film A", "Desc A", "/a.jpg", "/ba.jpg", "2024-01-01", 7.5, 800),
            MovieData(11, "Film B", "Desc B", null, null, "2023-06-01", 6.0, 200),
        )
        db.movieDetailCacheDao().insert(entity().copy(similarMovies = movies))
        db.movieDetailCacheDao().getById(1).test {
            assertEquals(movies, awaitItem()?.similarMovies)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nullable certification and trailer fields survive round-trip`() = runTest {
        db.movieDetailCacheDao().insert(
            entity().copy(certification = null, trailerKey = null, trailerName = null)
        )
        db.movieDetailCacheDao().getById(1).test {
            val item = awaitItem()
            assertNull(item?.certification)
            assertNull(item?.trailerKey)
            assertNull(item?.trailerName)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
