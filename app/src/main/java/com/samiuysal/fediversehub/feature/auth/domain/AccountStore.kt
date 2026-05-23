package com.samiuysal.fediversehub.feature.auth.domain

import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import kotlinx.coroutines.flow.Flow

interface AccountStore {
    val accounts: Flow<List<Account>>
    val activeAccountIds: Flow<Map<PlatformType, String>>

    suspend fun saveAccount(account: Account)
    suspend fun removeAccount(accountId: String)
    suspend fun saveActiveAccount(platform: PlatformType, accountId: String)
    suspend fun readPendingMastodonOAuthSession(): MastodonOAuthSession?
    suspend fun savePendingMastodonOAuthSession(session: MastodonOAuthSession)
    suspend fun clearPendingMastodonOAuthSession()
    suspend fun readPendingPixelfedOAuthSession(): PixelfedOAuthSession?
    suspend fun savePendingPixelfedOAuthSession(session: PixelfedOAuthSession)
    suspend fun clearPendingPixelfedOAuthSession()
}
