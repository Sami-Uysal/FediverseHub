package com.samiuysal.fediversehub.feature.pixelfed.domain

data class PixelfedPost(
    val id: String,
    val authorAccountId: String,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val createdAt: String?,
    val caption: String,
    val media: List<PixelfedMedia>,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
)

data class PixelfedMedia(
    val id: String,
    val previewUrl: String?,
    val fullUrl: String?,
    val description: String?,
)

data class PixelfedComment(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val text: String,
    val timeAgo: String,
)
