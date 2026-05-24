package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.feature.lemmy.data.mock.MockLemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyApi
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyKtorApi
import com.samiuysal.fediversehub.feature.lemmy.data.repository.LemmyRepositoryImpl
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RealLemmyRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockLemmyRepositoryBinding

@Module
@InstallIn(SingletonComponent::class)
abstract class LemmyModule {
    @Binds
    @Singleton
    abstract fun bindLemmyApi(api: LemmyKtorApi): LemmyApi

    @Binds
    @Singleton
    abstract fun bindLemmyRepository(repository: LemmyRepositoryImpl): LemmyRepository

    @Binds
    @Singleton
    @MockLemmyRepositoryBinding
    abstract fun bindMockLemmyRepository(repository: MockLemmyRepository): LemmyRepository

    @Binds
    @Singleton
    @RealLemmyRepository
    abstract fun bindRealLemmyRepository(
        repository: LemmyRepositoryImpl,
    ): LemmyRepository
}
