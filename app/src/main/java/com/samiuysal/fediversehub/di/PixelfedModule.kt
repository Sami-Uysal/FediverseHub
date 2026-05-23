package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedApi
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedKtorApi
import com.samiuysal.fediversehub.feature.pixelfed.data.repository.PixelfedRepositoryImpl
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PixelfedModule {
    @Binds
    @Singleton
    abstract fun bindPixelfedApi(
        api: PixelfedKtorApi,
    ): PixelfedApi

    @Binds
    @Singleton
    abstract fun bindPixelfedRepository(
        repository: PixelfedRepositoryImpl,
    ): PixelfedRepository
}
