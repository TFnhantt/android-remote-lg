package com.uni.remote.tech.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.uni.remote.tech.common.dispatcher.CoroutineDispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppCoroutineScope(dispatchers: CoroutineDispatchers) =
        CoroutineScope(dispatchers.io + SupervisorJob())
}
