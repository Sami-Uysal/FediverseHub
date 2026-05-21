package com.samiuysal.fediversehub.feature.pixelfed

import androidx.compose.runtime.Immutable

@Immutable
data class PixelfedPostUiModel(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val imageUrl: String,
    val caption: String,
    val likes: Int,
    val comments: Int,
    val timeAgo: String,
)
