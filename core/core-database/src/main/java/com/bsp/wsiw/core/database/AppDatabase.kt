package com.bsp.wsiw.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bsp.wsiw.core.database.converter.Converters
import com.bsp.wsiw.core.database.dao.MovieDetailCacheDao
import com.bsp.wsiw.core.database.dao.PopularMovieCacheDao
import com.bsp.wsiw.core.database.dao.WatchlistDao
import com.bsp.wsiw.core.database.entity.MovieDetailEntity
import com.bsp.wsiw.core.database.entity.PopularMovieEntity
import com.bsp.wsiw.core.database.entity.WatchlistEntity

@Database(
    entities = [WatchlistEntity::class, PopularMovieEntity::class, MovieDetailEntity::class],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun popularMovieCacheDao(): PopularMovieCacheDao
    abstract fun movieDetailCacheDao(): MovieDetailCacheDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `popular_movies_cache` (
                        `id` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `overview` TEXT NOT NULL,
                        `posterUrl` TEXT,
                        `backdropUrl` TEXT,
                        `releaseDate` TEXT NOT NULL,
                        `voteAverage` REAL NOT NULL,
                        `voteCount` INTEGER NOT NULL,
                        `page` INTEGER NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )""",
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `movie_detail_cache` (
                        `id` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `overview` TEXT NOT NULL,
                        `posterUrl` TEXT,
                        `backdropUrl` TEXT,
                        `releaseDate` TEXT NOT NULL,
                        `voteAverage` REAL NOT NULL,
                        `voteCount` INTEGER NOT NULL,
                        `tagline` TEXT NOT NULL,
                        `genres` TEXT NOT NULL,
                        `runtime` INTEGER NOT NULL,
                        `originalLanguage` TEXT NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )""",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movie_detail_cache ADD COLUMN trailerKey TEXT")
                db.execSQL("ALTER TABLE movie_detail_cache ADD COLUMN trailerName TEXT")
                db.execSQL("ALTER TABLE movie_detail_cache ADD COLUMN cast TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE movie_detail_cache ADD COLUMN similarMovies TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movie_detail_cache ADD COLUMN recommendedMovies TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE movie_detail_cache ADD COLUMN certification TEXT")
            }
        }
    }
}
