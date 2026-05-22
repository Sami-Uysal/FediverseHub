package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MastodonContextDto(
    val ancestors: List<MastodonStatusDto> = emptyList(),
    val descendants: List<MastodonStatusDto> = emptyList(),
)
