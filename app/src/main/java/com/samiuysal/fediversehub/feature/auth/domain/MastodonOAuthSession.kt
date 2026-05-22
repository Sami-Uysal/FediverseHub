package com.samiuysal.fediversehub.feature.auth.domain

import kotlinx.serialization.Serializable

@Serializable
data class MastodonOAuthSession(
    val instanceUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: String,
    val state: String,
)
