package com.bsp.wsiw.core.data.di

import com.bsp.wsiw.core.data.ratings.RatingRepositoryImpl
import com.bsp.wsiw.core.domain.repository.RatingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RatingDataModule {

    @Binds
    @Singleton
    abstract fun bindRatingRepository(impl: RatingRepositoryImpl): RatingRepository
}
