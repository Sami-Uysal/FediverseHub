package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MastodonSearchDto(
    val accounts: List<MastodonAccountDto> = emptyList(),
    val statuses: List<MastodonStatusDto> = emptyList(),
    val hashtags: List<MastodonHashtagDto> = emptyList(),
)

@Serializable
data class MastodonHashtagDto(
    val name: String,
    val url: String? = null,
)
