package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bsp.wsiw.core.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :movieId)")
    fun isWatchlisted(movieId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: WatchlistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE id = :movieId")
    fun deleteById(movieId: Int): Int

    @Query("DELETE FROM watchlist")
    fun deleteAll()

    @Transaction
    suspend fun replaceAll(movies: List<WatchlistEntity>) {
        deleteAll()
        insertAll(movies)
    }
}
