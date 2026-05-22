package com.samiuysal.fediversehub.feature.auth.domain

import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountStore {
    val accounts: Flow<List<Account>>

    suspend fun saveAccount(account: Account)
    suspend fun removeAccount(accountId: String)
    suspend fun readPendingMastodonOAuthSession(): MastodonOAuthSession?
    suspend fun savePendingMastodonOAuthSession(session: MastodonOAuthSession)
    suspend fun clearPendingMastodonOAuthSession()
}
