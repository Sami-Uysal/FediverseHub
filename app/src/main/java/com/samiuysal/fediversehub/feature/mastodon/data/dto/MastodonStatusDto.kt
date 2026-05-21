package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonStatusDto(
    val id: String,
    val uri: String? = null,
    val url: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val account: MastodonAccountDto,
    val content: String = "",
    @SerialName("media_attachments") val mediaAttachments: List<MastodonMediaAttachmentDto> = emptyList(),
    @SerialName("replies_count") val repliesCount: Int = 0,
    @SerialName("reblogs_count") val reblogsCount: Int = 0,
    @SerialName("favourites_count") val favouritesCount: Int = 0,
    val reblog: MastodonStatusDto? = null,
)
