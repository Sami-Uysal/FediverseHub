package com.samiuysal.fediversehub.feature.lemmy.domain

data class LemmyPost(
    val id: String,
    val title: String,
    val communityName: String,
    val communityActorId: String?,
    val domain: String?,
    val authorName: String,
    val publishedAt: String?,
    val score: Int,
    val commentCount: Int,
    val previewText: String,
    val comments: List<LemmyComment>,
)

data class LemmyComment(
    val id: String,
    val parentId: String?,
    val authorName: String,
    val content: String,
    val depth: Int,
    val isCollapsed: Boolean,
)

enum class LemmySortType {
    HOT,
    ACTIVE,
    NEW,
    TOP_DAY,
}
