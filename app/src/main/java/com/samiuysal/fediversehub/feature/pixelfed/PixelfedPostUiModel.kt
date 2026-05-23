package com.samiuysal.fediversehub.feature.pixelfed

import androidx.compose.runtime.Immutable

@Immutable
data class PixelfedPostUiModel(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val imageUrl: String,
    val fullImageUrls: List<String> = listOf(imageUrl),
    val altFlags: List<Boolean> = emptyList(),
    val caption: String,
    val likes: Int,
    val comments: Int,
    val timeAgo: String,
    val isLiked: Boolean = false,
    val isLoadingLike: Boolean = false,
)
