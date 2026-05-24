package com.samiuysal.fediversehub.core.datastore

/**
 * MVP 2 token plan:
 * - Access tokens are stored outside DataStore in SecureTokenStore.
 * - SecureTokenStore encrypts token bytes with Android Keystore backed AES-GCM.
 * - DataStore keeps non-sensitive account metadata and active-account IDs only.
 * - Legacy plain DataStore tokens are migrated on startup, then removed from account metadata.
 */
interface TokenStoragePlan {
    suspend fun readAccessToken(accountId: String): String?
    suspend fun writeAccessToken(accountId: String, token: String)
}
