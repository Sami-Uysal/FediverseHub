package com.samiuysal.fediversehub.feature.auth.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.samiuysal.fediversehub.core.datastore.SecureTokenStore
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.MastodonOAuthSession
import com.samiuysal.fediversehub.feature.auth.domain.PixelfedOAuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

@Singleton
class DataStoreAccountStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val secureTokenStore: SecureTokenStore,
) : AccountStore {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val migrationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        migrationScope.launch { migrateLegacyPlainTokens() }
    }

    override val accounts: Flow<List<Account>> = context.authDataStore.data.map { preferences ->
        preferences[ACCOUNTS_JSON]
            ?.decodeStoredAccounts()
            ?.map { stored ->
                stored.toDomain(
                    accessTokenOverride = secureTokenStore.readAccessToken(stored.id)
                        ?: stored.accessToken,
                )
            }
            .orEmpty()
    }

    override val activeAccountIds: Flow<Map<PlatformType, String>> =
        context.authDataStore.data.map { preferences ->
            preferences[ACTIVE_ACCOUNTS_JSON]
                ?.decodeActiveAccounts()
                .orEmpty()
        }

    override suspend fun saveAccount(account: Account) {
        account.accessToken?.let { token ->
            secureTokenStore.writeAccessToken(account.id, token)
        }
        context.authDataStore.edit { preferences ->
            val accounts = preferences[ACCOUNTS_JSON]
                ?.decodeStoredAccounts()
                .orEmpty()
                .filterNot { it.matches(account) }
                .plus(account.toStoredAccount())
            preferences[ACCOUNTS_JSON] = json.encodeToString(accounts)
        }
    }

    override suspend fun removeAccount(accountId: String) {
        context.authDataStore.edit { preferences ->
            val accounts = preferences[ACCOUNTS_JSON]
                ?.decodeStoredAccounts()
                .orEmpty()
                .filterNot { it.id == accountId }
            preferences[ACCOUNTS_JSON] = json.encodeToString(accounts)
            val activeAccounts = preferences[ACTIVE_ACCOUNTS_JSON]
                ?.decodeActiveAccounts()
                .orEmpty()
                .filterValues { it != accountId }
            preferences[ACTIVE_ACCOUNTS_JSON] = json.encodeToString(activeAccounts)
        }
        secureTokenStore.deleteAccessToken(accountId)
    }

    override suspend fun saveActiveAccount(platform: PlatformType, accountId: String) {
        context.authDataStore.edit { preferences ->
            val exists = preferences[ACCOUNTS_JSON]
                ?.decodeStoredAccounts()
                .orEmpty()
                .any { it.platform == platform && it.id == accountId }
            if (!exists) return@edit
            val activeAccounts = preferences[ACTIVE_ACCOUNTS_JSON]
                ?.decodeActiveAccounts()
                .orEmpty()
                .plus(platform to accountId)
            preferences[ACTIVE_ACCOUNTS_JSON] = json.encodeToString(activeAccounts)
        }
    }

    override suspend fun readPendingMastodonOAuthSession(): MastodonOAuthSession? {
        val preferences = context.authDataStore.data.first()
        return preferences[PENDING_MASTODON_OAUTH_JSON]?.let { value ->
            runCatching { json.decodeFromString<MastodonOAuthSession>(value) }.getOrNull()
        }
    }

    override suspend fun savePendingMastodonOAuthSession(session: MastodonOAuthSession) {
        context.authDataStore.edit { preferences ->
            preferences[PENDING_MASTODON_OAUTH_JSON] = json.encodeToString(session)
        }
    }

    override suspend fun clearPendingMastodonOAuthSession() {
        context.authDataStore.edit { preferences ->
            preferences.remove(PENDING_MASTODON_OAUTH_JSON)
        }
    }

    override suspend fun readPendingPixelfedOAuthSession(): PixelfedOAuthSession? {
        val preferences = context.authDataStore.data.first()
        return preferences[PENDING_PIXELFED_OAUTH_JSON]?.let { value ->
            runCatching { json.decodeFromString<PixelfedOAuthSession>(value) }.getOrNull()
        }
    }

    override suspend fun savePendingPixelfedOAuthSession(session: PixelfedOAuthSession) {
        context.authDataStore.edit { preferences ->
            preferences[PENDING_PIXELFED_OAUTH_JSON] = json.encodeToString(session)
        }
    }

    override suspend fun clearPendingPixelfedOAuthSession() {
        context.authDataStore.edit { preferences ->
            preferences.remove(PENDING_PIXELFED_OAUTH_JSON)
        }
    }

    private fun String.decodeStoredAccounts(): List<StoredAccount> =
        runCatching { json.decodeFromString<List<StoredAccount>>(this) }.getOrDefault(emptyList())

    private fun String.decodeActiveAccounts(): Map<PlatformType, String> =
        runCatching { json.decodeFromString<Map<PlatformType, String>>(this) }.getOrDefault(emptyMap())

    private fun StoredAccount.matches(account: Account): Boolean =
        platform == account.platform &&
            instanceUrl.normalizedInstance() == account.instanceUrl.normalizedInstance() &&
            (id == account.id || username.equals(account.username, ignoreCase = true))

    private fun String.normalizedInstance(): String =
        removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
            .lowercase()

    private suspend fun migrateLegacyPlainTokens() {
        context.authDataStore.edit { preferences ->
            val accounts = preferences[ACCOUNTS_JSON]
                ?.decodeStoredAccounts()
                .orEmpty()
            val migrated = accounts.map { stored ->
                stored.accessToken?.let { token ->
                    secureTokenStore.writeAccessToken(stored.id, token)
                }
                stored.copy(accessToken = null)
            }
            if (migrated != accounts) {
                preferences[ACCOUNTS_JSON] = json.encodeToString(migrated)
            }
        }
    }

    private companion object {
        val ACCOUNTS_JSON = stringPreferencesKey("accounts_json")
        val ACTIVE_ACCOUNTS_JSON = stringPreferencesKey("active_accounts_json")
        val PENDING_MASTODON_OAUTH_JSON = stringPreferencesKey("pending_mastodon_oauth_json")
        val PENDING_PIXELFED_OAUTH_JSON = stringPreferencesKey("pending_pixelfed_oauth_json")
    }
}
