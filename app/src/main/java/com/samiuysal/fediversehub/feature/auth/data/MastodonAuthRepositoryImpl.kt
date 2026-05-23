package com.samiuysal.fediversehub.feature.auth.data

import android.net.Uri
import androidx.room.withTransaction
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.database.AppDatabase
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.MastodonAuthRepository
import com.samiuysal.fediversehub.feature.auth.domain.MastodonOAuthSession
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonApi
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class MastodonAuthRepositoryImpl @Inject constructor(
    private val mastodonApi: MastodonApi,
    private val accountStore: AccountStore,
    private val database: AppDatabase,
) : MastodonAuthRepository {
    override val accounts: Flow<List<Account>> = accountStore.accounts

    override suspend fun startLogin(instanceUrl: String): AppResult<String> {
        val normalizedInstanceUrl = instanceUrl.normalizedHttpsBaseUrl()
        if (normalizedInstanceUrl == null) {
            return AppResult.Failure(AppError.Unknown("Enter a valid Mastodon instance URL."))
        }

        return runCatching {
            val app = mastodonApi.registerApp(
                instanceUrl = normalizedInstanceUrl,
                clientName = CLIENT_NAME,
                redirectUri = REDIRECT_URI,
                scopes = SCOPES,
                website = null,
            )
            val session = MastodonOAuthSession(
                instanceUrl = normalizedInstanceUrl,
                clientId = app.clientId,
                clientSecret = app.clientSecret,
                redirectUri = REDIRECT_URI,
                scopes = SCOPES,
                state = UUID.randomUUID().toString(),
            )
            accountStore.savePendingMastodonOAuthSession(session)
            AppResult.Success(session.authorizeUrl())
        }.getOrElse { throwable ->
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun handleCallback(callbackUrl: String): AppResult<Account> {
        val uri = Uri.parse(callbackUrl)
        val error = uri.getQueryParameter("error")
        if (error != null) {
            return AppResult.Failure(AppError.Unknown(error))
        }
        val code = uri.getQueryParameter("code")
            ?: return AppResult.Failure(AppError.Unknown("OAuth callback did not contain a code."))
        val state = uri.getQueryParameter("state")
        val session = accountStore.readPendingMastodonOAuthSession()
            ?: return AppResult.Failure(AppError.Unknown("No pending Mastodon login session."))

        if (state != session.state) {
            return AppResult.Failure(AppError.Unauthorized)
        }

        return runCatching {
            val token = mastodonApi.exchangeCodeForToken(
                instanceUrl = session.instanceUrl,
                clientId = session.clientId,
                clientSecret = session.clientSecret,
                redirectUri = session.redirectUri,
                code = code,
                scopes = session.scopes,
            )
            val credentials = mastodonApi.verifyCredentials(
                instanceUrl = session.instanceUrl,
                accessToken = token.accessToken,
            )
            val account = Account(
                id = "mastodon-${session.instanceUrl}-${credentials.id}",
                platform = PlatformType.MASTODON,
                instanceUrl = session.instanceUrl.removePrefix("https://"),
                username = credentials.acct.ifBlank { credentials.username },
                displayName = credentials.displayName.ifBlank { credentials.username },
                avatarUrl = credentials.avatarStatic ?: credentials.avatar,
                accessToken = token.accessToken,
            )
            accountStore.saveAccount(account)
            accountStore.saveActiveAccount(account.platform, account.id)
            accountStore.clearPendingMastodonOAuthSession()
            AppResult.Success(account)
        }.getOrElse { throwable ->
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun logout(accountId: String): AppResult<Unit> = runCatching {
        accountStore.removeAccount(accountId)
        database.withTransaction {
            val mastodonTimelineDao = database.mastodonTimelineDao()
            mastodonTimelineDao.clearRemoteKey(accountId)
            mastodonTimelineDao.clearTimeline(accountId)
        }
        AppResult.Success(Unit)
    }.getOrElse { throwable ->
        AppResult.Failure(AppError.Unknown(throwable.message))
    }

    private fun MastodonOAuthSession.authorizeUrl(): String =
        "$instanceUrl/oauth/authorize" +
            "?client_id=${clientId.urlEncoded()}" +
            "&redirect_uri=${redirectUri.urlEncoded()}" +
            "&response_type=code" +
            "&scope=${scopes.urlEncoded()}" +
            "&state=${state.urlEncoded()}"

    private fun String.urlEncoded(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun String.normalizedHttpsBaseUrl(): String? {
        val trimmed = trim().trimEnd('/')
        if (trimmed.isBlank()) return null
        return when {
            trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("http://") -> trimmed.replaceFirst("http://", "https://")
            else -> "https://$trimmed"
        }
    }

    private companion object {
        const val CLIENT_NAME = "FediverseHub"
        const val REDIRECT_URI = "fediversehub://oauth/mastodon"
        const val SCOPES = "read write follow"
    }
}
