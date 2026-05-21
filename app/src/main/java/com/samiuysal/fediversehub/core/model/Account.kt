package com.samiuysal.fediversehub.core.model

data class Account(
    val id: String,
    val platform: PlatformType,
    val instanceUrl: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val accessToken: String?,
)
