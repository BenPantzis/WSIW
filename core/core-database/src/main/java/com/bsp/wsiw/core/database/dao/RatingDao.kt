package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.bsp.wsiw.core.database.entity.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RatingDao {
    @Query("SELECT rating FROM ratings WHERE movieId = :movieId")
    abstract fun getRating(movieId: Int): Flow<Float?>

    @Query("SELECT * FROM ratings")
    abstract fun getAll(): Flow<List<RatingEntity>>

    @Insert(onConflict = REPLACE)
    abstract fun upsert(entity: RatingEntity): Long

    @Insert(onConflict = REPLACE)
    abstract suspend fun insertAll(entities: List<RatingEntity>)

    @Query("DELETE FROM ratings WHERE movieId = :movieId")
    abstract fun delete(movieId: Int): Int

    @Query("DELETE FROM ratings")
    abstract suspend fun deleteAll()
}
