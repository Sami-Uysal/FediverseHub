package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.feature.auth.data.DataStoreAccountStore
import com.samiuysal.fediversehub.feature.auth.data.LemmyAuthRepositoryImpl
import com.samiuysal.fediversehub.feature.auth.data.MastodonAuthRepositoryImpl
import com.samiuysal.fediversehub.feature.auth.data.PixelfedAuthRepositoryImpl
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.LemmyAuthRepository
import com.samiuysal.fediversehub.feature.auth.domain.MastodonAuthRepository
import com.samiuysal.fediversehub.feature.auth.domain.PixelfedAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAccountStore(
        store: DataStoreAccountStore,
    ): AccountStore

    @Binds
    @Singleton
    abstract fun bindMastodonAuthRepository(
        repository: MastodonAuthRepositoryImpl,
    ): MastodonAuthRepository

    @Binds
    @Singleton
    abstract fun bindPixelfedAuthRepository(
        repository: PixelfedAuthRepositoryImpl,
    ): PixelfedAuthRepository

    @Binds
    @Singleton
    abstract fun bindLemmyAuthRepository(
        repository: LemmyAuthRepositoryImpl,
    ): LemmyAuthRepository
}
