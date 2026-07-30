package com.bsp.wsiw.core.data.di

import com.bsp.wsiw.core.data.watchlist.WatchlistRepositoryImpl
import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WatchlistDataModule {

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository
}
