package com.samiuysal.fediversehub.feature.auth.data

import android.util.Log
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.LemmyAuthRepository
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class LemmyAuthRepositoryImpl @Inject constructor(
    private val lemmyApi: LemmyApi,
    private val accountStore: AccountStore,
) : LemmyAuthRepository {
    override val accounts: Flow<List<Account>> = accountStore.accounts

    override suspend fun login(
        instanceUrl: String,
        usernameOrEmail: String,
        password: String,
    ): AppResult<Account> {
        val cleanInstance = instanceUrl.normalizedInstance()
        val cleanUser = usernameOrEmail.trim()
        if (cleanInstance.isBlank() || cleanUser.isBlank() || password.isBlank()) {
            return AppResult.Failure(AppError.Unknown("Instance, username and password required."))
        }

        return try {
            val response = lemmyApi.login(
                instanceUrl = cleanInstance,
                request = LemmyLoginRequestDto(
                    usernameOrEmail = cleanUser,
                    password = password,
                ),
            )
            val jwt = response.jwt?.takeIf { it.isNotBlank() }
                ?: return AppResult.Failure(AppError.Unauthorized)
            val account = Account(
                id = "lemmy-${cleanInstance.lowercase()}-${cleanUser.lowercase()}",
                platform = PlatformType.LEMMY,
                instanceUrl = cleanInstance,
                username = cleanUser,
                displayName = cleanUser,
                avatarUrl = null,
                accessToken = jwt,
            )
            accountStore.saveAccount(account)
            accountStore.saveActiveAccount(PlatformType.LEMMY, account.id)
            AppResult.Success(account)
        } catch (throwable: Throwable) {
            Log.d(TAG, "Lemmy login failed: ${throwable.message}", throwable)
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun logout(accountId: String): AppResult<Unit> =
        try {
            accountStore.removeAccount(accountId)
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    private fun String.normalizedInstance(): String =
        trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')

    private companion object {
        const val TAG = "LemmyAuthRepository"
    }
}
