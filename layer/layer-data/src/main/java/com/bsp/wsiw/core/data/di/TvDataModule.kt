package com.bsp.wsiw.core.data.di

import com.bsp.wsiw.core.data.tv.TvRepositoryImpl
import com.bsp.wsiw.core.domain.repository.TvRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TvDataModule {

    @Binds
    @Singleton
    abstract fun bindTvRepository(impl: TvRepositoryImpl): TvRepository
}
