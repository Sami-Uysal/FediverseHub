package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonTokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String? = null,
    val scope: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
)
