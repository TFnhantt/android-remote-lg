package com.uni.remote.tech.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.uni.remote.tech.common.bus.BusProvider
import com.uni.remote.tech.common.bus.BusProviderImpl
import com.uni.remote.tech.common.dispatcher.AppCoroutineDispatchers
import com.uni.remote.tech.common.dispatcher.CoroutineDispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Singleton
    @Provides
    fun provideBusProvider(): BusProvider = BusProviderImpl()

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers = AppCoroutineDispatchers()
}