package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDetailCacheDao {

    @Query("SELECT * FROM movie_detail_cache WHERE id = :movieId")
    fun getById(movieId: Int): Flow<MovieDetailEntity?>

    @Query("SELECT * FROM movie_detail_cache WHERE id = :movieId")
    fun getByIdSync(movieId: Int): MovieDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: MovieDetailEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIfAbsent(entity: MovieDetailEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllIfAbsent(entities: List<MovieDetailEntity>)

    @Query("DELETE FROM movie_detail_cache WHERE id = :movieId")
    fun deleteById(movieId: Int): Int
}
