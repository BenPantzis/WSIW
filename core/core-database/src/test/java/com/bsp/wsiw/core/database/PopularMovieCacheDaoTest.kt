package com.bsp.wsiw.core.database

import app.cash.turbine.test
import com.bsp.wsiw.core.database.entity.PopularMovieEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PopularMovieCacheDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = buildTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(id: Int, page: Int = 1) = PopularMovieEntity(
        id = id,
        title = "Movie $id",
        overview = "Overview $id",
        posterUrl = "/poster$id.jpg",
        backdropUrl = null,
        releaseDate = "2024-01-01",
        voteAverage = 7.0,
        voteCount = 500,
        page = page,
        cachedAt = System.currentTimeMillis(),
    )

    @Test
    fun `insertAll then getByPage returns entities for that page`() = runTest {
        db.popularMovieCacheDao().insertAll(listOf(entity(1, page = 1), entity(2, page = 1)))
        db.popularMovieCacheDao().getByPage(1).test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByPage returns empty for page with no entries`() = runTest {
        db.popularMovieCacheDao().insertAll(listOf(entity(1, page = 1)))
        db.popularMovieCacheDao().getByPage(2).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByPage isolates pages from each other`() = runTest {
        db.popularMovieCacheDao().insertAll(
            listOf(entity(1, page = 1), entity(2, page = 1), entity(3, page = 2))
        )
        db.popularMovieCacheDao().getByPage(2).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(3, items.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertAll replaces existing entry with same id`() = runTest {
        db.popularMovieCacheDao().insertAll(listOf(entity(1, page = 1).copy(title = "Original")))
        db.popularMovieCacheDao().insertAll(listOf(entity(1, page = 1).copy(title = "Replaced")))
        db.popularMovieCacheDao().getByPage(1).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Replaced", items.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteByPage removes only that page`() = runTest {
        db.popularMovieCacheDao().insertAll(
            listOf(entity(1, page = 1), entity(2, page = 2))
        )
        db.popularMovieCacheDao().deleteByPage(1)
        db.popularMovieCacheDao().getByPage(1).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        db.popularMovieCacheDao().getByPage(2).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteByPage returns row count deleted`() {
        db.popularMovieCacheDao().insertAll(listOf(entity(1, page = 3), entity(2, page = 3)))
        val rows = db.popularMovieCacheDao().deleteByPage(3)
        assertEquals(2, rows)
    }

    @Test
    fun `deleteByPage returns 0 when page does not exist`() {
        val rows = db.popularMovieCacheDao().deleteByPage(99)
        assertEquals(0, rows)
    }

    @Test
    fun `getByPage emits updated list after new insertAll`() = runTest {
        db.popularMovieCacheDao().getByPage(1).test {
            assertTrue(awaitItem().isEmpty())
            db.popularMovieCacheDao().insertAll(listOf(entity(1, page = 1)))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
