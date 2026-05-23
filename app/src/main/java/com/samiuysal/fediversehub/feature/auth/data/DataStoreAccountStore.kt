package com.samiuysal.fediversehub.feature.auth.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.MastodonOAuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

@Singleton
class DataStoreAccountStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AccountStore {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override val accounts: Flow<List<Account>> = context.authDataStore.data.map { preferences ->
        preferences[ACCOUNTS_JSON]
            ?.decodeStoredAccounts()
            ?.map(StoredAccount::toDomain)
            .orEmpty()
    }

    override val activeAccountIds: Flow<Map<PlatformType, String>> =
        context.authDataStore.data.map { preferences ->
            preferences[ACTIVE_ACCOUNTS_JSON]
                ?.decodeActiveAccounts()
                .orEmpty()
        }

    override suspend fun saveAccount(account: Account) {
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
    }

    override suspend fun saveActiveAccount(platform: PlatformType, accountId: String) {
        context.authDataStore.edit { preferences ->
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

    private companion object {
        val ACCOUNTS_JSON = stringPreferencesKey("accounts_json")
        val ACTIVE_ACCOUNTS_JSON = stringPreferencesKey("active_accounts_json")
        val PENDING_MASTODON_OAUTH_JSON = stringPreferencesKey("pending_mastodon_oauth_json")
    }
}
