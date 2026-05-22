package com.samiuysal.fediversehub.feature.auth.data

import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import kotlinx.serialization.Serializable

@Serializable
data class StoredAccount(
    val id: String,
    val platform: PlatformType,
    val instanceUrl: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val accessToken: String?,
) {
    fun toDomain(): Account = Account(
        id = id,
        platform = platform,
        instanceUrl = instanceUrl,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        accessToken = accessToken,
    )
}

fun Account.toStoredAccount(): StoredAccount = StoredAccount(
    id = id,
    platform = platform,
    instanceUrl = instanceUrl,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    accessToken = accessToken,
)
