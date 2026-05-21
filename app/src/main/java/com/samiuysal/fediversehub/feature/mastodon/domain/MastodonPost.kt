package com.samiuysal.fediversehub.feature.mastodon.domain

data class MastodonPost(
    val id: String,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val createdAt: String?,
    val contentText: String,
    val mediaAttachments: List<MastodonMediaAttachment>,
    val replyCount: Int,
    val reblogCount: Int,
    val favouriteCount: Int,
    val url: String?,
)

data class MastodonMediaAttachment(
    val id: String,
    val type: MastodonMediaType,
    val url: String?,
    val previewUrl: String?,
    val description: String?,
)

enum class MastodonMediaType {
    IMAGE,
    GIFV,
    VIDEO,
    AUDIO,
    UNKNOWN,
}
