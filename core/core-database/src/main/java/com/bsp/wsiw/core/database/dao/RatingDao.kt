package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.bsp.wsiw.core.database.entity.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Query("SELECT rating FROM ratings WHERE movieId = :movieId")
    fun getRating(movieId: Int): Flow<Float?>

    @Query("SELECT * FROM ratings")
    fun getAll(): Flow<List<RatingEntity>>

    @Insert(onConflict = REPLACE)
    fun upsert(entity: RatingEntity)

    @Insert(onConflict = REPLACE)
    fun insertAll(entities: List<RatingEntity>)

    @Query("DELETE FROM ratings WHERE movieId = :movieId")
    fun delete(movieId: Int)

    @Query("DELETE FROM ratings")
    fun deleteAll()

    @Transaction
    suspend fun replaceAll(ratings: List<RatingEntity>) {
        deleteAll()
        insertAll(ratings)
    }
}
