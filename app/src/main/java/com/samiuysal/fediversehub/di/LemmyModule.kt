package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.feature.lemmy.data.mock.MockLemmyRepository
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

@Module
@InstallIn(SingletonComponent::class)
abstract class LemmyModule {
    @Binds
    @Singleton
    abstract fun bindMockLemmyRepository(
        repository: MockLemmyRepository,
    ): LemmyRepository

    @Binds
    @Singleton
    @RealLemmyRepository
    abstract fun bindRealLemmyRepository(
        repository: LemmyRepositoryImpl,
    ): LemmyRepository
}
