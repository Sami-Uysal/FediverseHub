package com.samiuysal.fediversehub.di

import com.samiuysal.fediversehub.feature.auth.data.DataStoreAccountStore
import com.samiuysal.fediversehub.feature.auth.data.MastodonAuthRepositoryImpl
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.MastodonAuthRepository
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
}
