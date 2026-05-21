package com.samiuysal.fediversehub.feature.lemmy

import androidx.compose.runtime.Immutable

@Immutable
data class LemmyPostUiModel(
    val id: String,
    val title: String,
    val community: String,
    val domain: String?,
    val author: String,
    val timeAgo: String,
    val score: Int,
    val comments: Int,
    val previewText: String,
    val nestedComments: List<CommentUiModel>,
)

@Immutable
data class CommentUiModel(
    val id: String,
    val parentId: String?,
    val author: String,
    val text: String,
    val depth: Int,
    val isCollapsed: Boolean,
)
