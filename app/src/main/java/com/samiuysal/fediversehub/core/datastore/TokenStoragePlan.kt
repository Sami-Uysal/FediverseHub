package com.samiuysal.fediversehub.core.datastore

/**
 * MVP 2 token plan:
 * - Current implementation stores the Mastodon access token with account metadata in DataStore.
 * - Before broad beta, move access tokens to Android Keystore backed encrypted storage.
 * - Keep DataStore for non-sensitive account metadata and encrypted-token lookup keys.
 */
interface TokenStoragePlan {
    suspend fun readAccessToken(accountId: String): String?
    suspend fun writeAccessToken(accountId: String, token: String)
}
