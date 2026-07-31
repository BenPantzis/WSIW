package com.bsp.wsiw.core.database

import app.cash.turbine.test
import com.bsp.wsiw.core.database.entity.WatchlistEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WatchlistDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = buildTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        id: Int = 1,
        title: String = "Test Movie",
        addedAt: Long = 1_000L,
    ) = WatchlistEntity(
        id = id,
        title = title,
        overview = "An overview",
        posterUrl = "/poster.jpg",
        backdropUrl = null,
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 1000,
        addedAt = addedAt,
    )

    @Test
    fun `insert then getAll returns entity`() = runTest {
        db.watchlistDao().insert(entity())
        db.watchlistDao().getAll().test {
            assertEquals(listOf(entity()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns empty list initially`() = runTest {
        db.watchlistDao().getAll().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert with same id replaces existing entry`() = runTest {
        db.watchlistDao().insert(entity(id = 1, title = "Original"))
        db.watchlistDao().insert(entity(id = 1, title = "Replaced"))
        db.watchlistDao().getAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Replaced", list.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll orders entries by addedAt descending`() = runTest {
        db.watchlistDao().insert(entity(id = 1, title = "Older", addedAt = 1_000L))
        db.watchlistDao().insert(entity(id = 2, title = "Newer", addedAt = 2_000L))
        db.watchlistDao().getAll().test {
            val list = awaitItem()
            assertEquals("Newer", list[0].title)
            assertEquals("Older", list[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isWatchlisted returns true after insert`() = runTest {
        db.watchlistDao().insert(entity(id = 42))
        db.watchlistDao().isWatchlisted(42).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isWatchlisted returns false for unknown id`() = runTest {
        db.watchlistDao().isWatchlisted(999).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isWatchlisted emits false after deleteById`() = runTest {
        db.watchlistDao().insert(entity(id = 5))
        db.watchlistDao().isWatchlisted(5).test {
            assertTrue(awaitItem())
            db.watchlistDao().deleteById(5)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteById returns 1 for existing entry`() {
        db.watchlistDao().insert(entity(id = 7))
        val rows = db.watchlistDao().deleteById(7)
        assertEquals(1, rows)
    }

    @Test
    fun `deleteById returns 0 for unknown id`() {
        val rows = db.watchlistDao().deleteById(999)
        assertEquals(0, rows)
    }

    @Test
    fun `deleteAll removes all entries`() = runTest {
        db.watchlistDao().insert(entity(id = 1))
        db.watchlistDao().insert(entity(id = 2))
        db.watchlistDao().deleteAll()
        db.watchlistDao().getAll().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nullable posterUrl and backdropUrl survive round-trip`() = runTest {
        val e = entity().copy(posterUrl = null, backdropUrl = null)
        db.watchlistDao().insert(e)
        db.watchlistDao().getAll().test {
            val item = awaitItem().first()
            assertNull(item.posterUrl)
            assertNull(item.backdropUrl)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll emits updated list after insert`() = runTest {
        db.watchlistDao().getAll().test {
            assertTrue(awaitItem().isEmpty())
            db.watchlistDao().insert(entity(id = 1))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
