package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bsp.wsiw.core.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    abstract fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :movieId)")
    abstract fun isWatchlisted(movieId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(entity: WatchlistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE id = :movieId")
    abstract fun deleteById(movieId: Int): Int

    @Query("DELETE FROM watchlist")
    abstract suspend fun deleteAll()
}
