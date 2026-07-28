package com.bsp.wsiw.core.database.di

import android.content.Context
import androidx.room.Room
import com.bsp.wsiw.core.database.AppDatabase
import com.bsp.wsiw.core.database.dao.MovieDetailCacheDao
import com.bsp.wsiw.core.database.dao.PopularMovieCacheDao
import com.bsp.wsiw.core.database.dao.WatchlistDao
import com.bsp.wsiw.core.database.watchlist.WatchlistRepositoryImpl
import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository

    companion object {

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "app_database",
            )
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()

        @Provides
        fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

        @Provides
        fun providePopularMovieCacheDao(db: AppDatabase): PopularMovieCacheDao = db.popularMovieCacheDao()

        @Provides
        fun provideMovieDetailCacheDao(db: AppDatabase): MovieDetailCacheDao = db.movieDetailCacheDao()
    }
}
