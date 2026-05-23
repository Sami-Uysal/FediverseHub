package com.samiuysal.fediversehub.feature.auth.domain

import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface PixelfedAuthRepository {
    val accounts: Flow<List<Account>>

    suspend fun startLogin(instanceUrl: String): AppResult<String>
    suspend fun handleCallback(callbackUrl: String): AppResult<Account>
    suspend fun logout(accountId: String): AppResult<Unit>
}
