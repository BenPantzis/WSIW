package com.bsp.wsiw.core.database

import app.cash.turbine.test
import com.bsp.wsiw.core.database.entity.RatingEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RatingDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = buildTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(movieId: Int, rating: Float = 7f) =
        RatingEntity(movieId = movieId, rating = rating, ratedAt = 1_000L)

    @Test
    fun `upsert then getRating returns correct value`() = runTest {
        db.ratingDao().upsert(entity(movieId = 1, rating = 8f))
        db.ratingDao().getRating(1).test {
            assertEquals(8f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRating returns null for unrated movie`() = runTest {
        db.ratingDao().getRating(999).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `upsert same movieId replaces rating`() = runTest {
        db.ratingDao().upsert(entity(movieId = 1, rating = 6f))
        db.ratingDao().upsert(entity(movieId = 1, rating = 9f))
        db.ratingDao().getRating(1).test {
            assertEquals(9f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRating emits updated value after upsert`() = runTest {
        db.ratingDao().upsert(entity(movieId = 1, rating = 5f))
        db.ratingDao().getRating(1).test {
            assertEquals(5f, awaitItem())
            db.ratingDao().upsert(entity(movieId = 1, rating = 10f))
            assertEquals(10f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete removes rating and getRating emits null`() = runTest {
        db.ratingDao().upsert(entity(movieId = 2))
        db.ratingDao().getRating(2).test {
            assertEquals(7f, awaitItem())
            db.ratingDao().delete(2)
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete on unknown movieId does not throw`() {
        db.ratingDao().delete(999)
    }

    @Test
    fun `getAll returns all rated movies`() = runTest {
        db.ratingDao().upsert(entity(movieId = 1, rating = 7f))
        db.ratingDao().upsert(entity(movieId = 2, rating = 9f))
        db.ratingDao().getAll().test {
            val all = awaitItem()
            assertEquals(2, all.size)
            val map = all.associateBy { it.movieId }
            assertEquals(7f, map[1]?.rating)
            assertEquals(9f, map[2]?.rating)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns empty list initially`() = runTest {
        db.ratingDao().getAll().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertAll upserts multiple ratings`() = runTest {
        db.ratingDao().insertAll(
            listOf(entity(movieId = 10, rating = 6f), entity(movieId = 11, rating = 8f))
        )
        db.ratingDao().getAll().test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAll clears all ratings`() = runTest {
        db.ratingDao().insertAll(listOf(entity(1), entity(2)))
        db.ratingDao().deleteAll()
        db.ratingDao().getAll().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
