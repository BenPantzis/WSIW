package com.bsp.wsiw.core.data.di

import com.bsp.wsiw.core.data.movie.MovieRepositoryImpl
import com.bsp.wsiw.core.data.remote.TmdbApiService
import com.bsp.wsiw.core.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MovieModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    companion object {
        @Provides
        @Singleton
        fun provideTmdbApiService(retrofit: Retrofit): TmdbApiService =
            retrofit.create(TmdbApiService::class.java)
    }
}
