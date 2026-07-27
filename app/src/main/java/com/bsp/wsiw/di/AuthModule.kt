package com.bsp.wsiw.di

import com.bsp.wsiw.auth.TmdbTokenProvider
import com.bsp.wsiw.core.common.auth.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: TmdbTokenProvider): TokenProvider
}
