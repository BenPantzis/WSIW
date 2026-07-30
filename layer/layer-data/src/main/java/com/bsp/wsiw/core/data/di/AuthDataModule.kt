package com.bsp.wsiw.core.data.di

import com.bsp.wsiw.core.data.auth.AuthRepositoryImpl
import com.bsp.wsiw.core.data.auth.DataStoreSessionRepository
import com.bsp.wsiw.core.data.remote.TmdbAuthService
import com.bsp.wsiw.core.domain.repository.AuthRepository
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.network.di.AuthRetrofit
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: DataStoreSessionRepository): SessionRepository

    companion object {
        @Provides
        @Singleton
        fun provideTmdbAuthService(@AuthRetrofit retrofit: Retrofit): TmdbAuthService =
            retrofit.create(TmdbAuthService::class.java)
    }
}
