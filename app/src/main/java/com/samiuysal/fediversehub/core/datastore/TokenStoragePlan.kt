package com.samiuysal.fediversehub.core.datastore

interface TokenStoragePlan {
    suspend fun readAccessToken(accountId: String): String?
    suspend fun writeAccessToken(accountId: String, token: String)
}
