package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonNotificationDto(
    val id: String,
    val type: String,
    @SerialName("created_at") val createdAt: String? = null,
    val account: MastodonAccountDto,
    val status: MastodonStatusDto? = null,
)
