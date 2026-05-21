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
    val replies: Int,
    val boosts: Int,
    val favourites: Int,
)
