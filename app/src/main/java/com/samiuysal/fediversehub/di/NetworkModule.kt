package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.BuildConfig
import com.samiuysal.fediversehub.core.network.FediverseHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = FediverseHttpClient.create(BuildConfig.DEBUG)
}
