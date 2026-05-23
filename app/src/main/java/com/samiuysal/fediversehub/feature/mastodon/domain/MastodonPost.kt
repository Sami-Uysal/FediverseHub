package com.samiuysal.fediversehub.feature.mastodon.domain

data class MastodonPost(
    val id: String,
    val detailId: String = id,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val createdAt: String?,
    val contentText: String,
    val mediaAttachments: List<MastodonMediaAttachment>,
    val boostedByDisplayName: String? = null,
    val boostedByAvatarUrl: String? = null,
    val inReplyToAccountId: String? = null,
    val linkPreview: MastodonLinkPreview? = null,
    val replyCount: Int,
    val reblogCount: Int,
    val favouriteCount: Int,
    val isReblogged: Boolean = false,
    val isFavourited: Boolean = false,
    val isBookmarked: Boolean = false,
    val visibility: String = "public",
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

data class MastodonLinkPreview(
    val domain: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
)

data class MastodonPostDetail(
    val post: MastodonPost,
    val ancestors: List<MastodonPost>,
    val descendants: List<MastodonPost>,
)
