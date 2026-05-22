package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.runtime.Immutable

@Immutable
data class MastodonPostUiModel(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val timeAgo: String,
    val content: String,
    val mediaUrl: String?,
    val hasAltText: Boolean = false,
    val boostedByDisplayName: String? = null,
    val boostedByAvatarUrl: String? = null,
    val replyContext: String? = null,
    val showThreadLine: Boolean = false,
    val linkPreview: MastodonLinkPreviewUiModel? = null,
    val replies: Int,
    val boosts: Int,
    val favourites: Int,
)

@Immutable
data class MastodonLinkPreviewUiModel(
    val domain: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
)
