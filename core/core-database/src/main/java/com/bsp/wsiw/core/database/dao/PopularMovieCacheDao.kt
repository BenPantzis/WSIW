package com.bsp.wsiw.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bsp.wsiw.core.database.entity.PopularMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PopularMovieCacheDao {

    @Query("SELECT * FROM popular_movies_cache WHERE page = :page ORDER BY id")
    fun getByPage(page: Int): Flow<List<PopularMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<PopularMovieEntity>)

    @Query("DELETE FROM popular_movies_cache WHERE page = :page")
    fun deleteByPage(page: Int): Int
}
