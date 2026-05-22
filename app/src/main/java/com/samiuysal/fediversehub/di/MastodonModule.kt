package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.feature.mastodon.data.mock.MockMastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonApi
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonKtorApi
import com.samiuysal.fediversehub.feature.mastodon.data.repository.MastodonRepositoryImpl
import com.samiuysal.fediversehub.feature.mastodon.data.repository.SwitchingMastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RealMastodonRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockMastodonRepositoryBinding

@Module
@InstallIn(SingletonComponent::class)
abstract class MastodonModule {
    @Binds
    @Singleton
    abstract fun bindMastodonApi(
        api: MastodonKtorApi,
    ): MastodonApi

    @Binds
    @Singleton
    abstract fun bindMastodonRepository(
        repository: SwitchingMastodonRepository,
    ): MastodonRepository

    @Binds
    @Singleton
    @MockMastodonRepositoryBinding
    abstract fun bindMockMastodonRepository(
        repository: MockMastodonRepository,
    ): MastodonRepository

    @Binds
    @Singleton
    @RealMastodonRepository
    abstract fun bindRealMastodonRepository(
        repository: MastodonRepositoryImpl,
    ): MastodonRepository
}
