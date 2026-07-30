package com.bsp.wsiw.di

import com.bsp.wsiw.BuildConfig
import com.bsp.wsiw.core.datastore.di.StaticToken
import com.bsp.wsiw.core.network.di.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @StaticToken
    fun provideStaticToken(): String = BuildConfig.TMDB_ACCESS_TOKEN
}
