package com.samiuysal.fediversehub.feature.auth.domain

import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface LemmyAuthRepository {
    val accounts: Flow<List<Account>>

    suspend fun login(
        instanceUrl: String,
        usernameOrEmail: String,
        password: String,
    ): AppResult<Account>

    suspend fun logout(accountId: String): AppResult<Unit>
}
