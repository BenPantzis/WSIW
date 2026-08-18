package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MovieDetailCacheDao {

    @Query("SELECT * FROM movie_detail_cache WHERE id = :movieId")
    abstract fun getById(movieId: Int): Flow<MovieDetailEntity?>

    @Query("SELECT * FROM movie_detail_cache WHERE id = :movieId")
    abstract fun getByIdSync(movieId: Int): MovieDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(entity: MovieDetailEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertIfAbsent(entity: MovieDetailEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertAllIfAbsent(entities: List<MovieDetailEntity>): List<Long>

    @Query("DELETE FROM movie_detail_cache WHERE id = :movieId")
    abstract fun deleteById(movieId: Int): Int
}
